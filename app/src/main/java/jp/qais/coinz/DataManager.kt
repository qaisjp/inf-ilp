package jp.qais.coinz

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import timber.log.Timber
import java.lang.AssertionError
import java.lang.Math.min
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * DataManager performs data requests from the FirebaseFirestore instance.
 *
 * In the future this could implement some sort of data provider interface that can be hot swapped
 * to support different document stores.
 */
object DataManager {
    private lateinit var coins: MutableSet<Coin>
    private lateinit var friends: MutableSet<Friend>
    private var accounts: MutableList<Account> = mutableListOf()
    private lateinit var goldAccount: Account

    data class Rates(val SHIL: Double, val DOLR: Double, val QUID: Double, val PENY: Double) {
        companion object {
            fun fromMap(map: Map<String, Any>): Rates {
                return Rates(map["SHIL"] as Double, map["DOLR"] as Double, map["QUID"] as Double, map["PENY"] as Double)
            }
        }

        fun toMap(): Map<String,Double> {
            return mapOf(
                    "SHIL" to SHIL,
                    "DOLR" to DOLR,
                    "QUID" to QUID,
                    "PENY" to PENY
            )
        }
    }
    private lateinit var rates: Rates
    private lateinit var name: String
    private lateinit var email: String

    private const val SPARE_CHANGE_THRESHOLD = 25

    /**
     * Determines whether or not payments are enabled (with a backing field for optimisation)
     */
    private var _coinsBankedToday: Int = 0
    private var coinsBankedToday: Int
        get() = _coinsBankedToday
        set(value) {
            // Don't do anything if it's already the correct value
            if (value == _coinsBankedToday) return

            Timber.d("coinsBankedToday now set to %s", value)
            getUserDocument().update("coinsBankedToday", value)
            _coinsBankedToday = value
        }

    private fun arePaymentsEnabled(num: Int) = num >= SPARE_CHANGE_THRESHOLD
    fun arePaymentsEnabled() = arePaymentsEnabled(coinsBankedToday)
    fun getCoinsUntilSpareChange() = SPARE_CHANGE_THRESHOLD - coinsBankedToday

    private const val COLLECTION_MAP = "map"
    private const val COLLECTION_IN = "coinsIn"
    private const val COLLECTION_FRIENDS = "friends"

    private fun store() = FirebaseFirestore.getInstance()

    fun getUserID() = FirebaseAuth.getInstance().currentUser!!.uid
    fun getUserEmail() = email
    fun getName() = name
    private fun getUserDocument(id: String) = store().document("users/$id")
    private fun getUserDocument() = getUserDocument(getUserID())
    private fun getAccountCollection(currency: Currency) = getUserDocument().collection("accounts-$currency")
    fun getAccount(currency: Currency) = accounts.first { it.currency == currency }
    fun getRates() = rates

    /**
     * Get coins as a non-mutable set
     */
    fun getCoins() = coins as Set<Coin>

    /**
     * Get accounts as a non-mutable list
     */
    fun getAccounts() = accounts as List<Account>

    /**
     * Get friends as a non-mutable set
     */
    fun getFriends() = friends as Set<Friend>

    /**
     * Delete coins and perform the relevant server updates
     */
    fun removeCoins(toRemove: Array<out Coin>) {
        coins.removeAll(toRemove)

        val batch = store().batch()
        val collection = getUserDocument().collection(COLLECTION_MAP)
        for (coin in toRemove) {
            batch.delete(collection.document(coin.id))
        }

        batch.commit().addOnFailureListener { throw it }
                .addOnSuccessListener {
                    Timber.d("SYNC SUCCESS BOI")
                }
    }

    /**
     * Fetches a new set of coins from the server
     */
    private fun fetchCoins(callback: () -> Unit) {
        getUserDocument().collection(COLLECTION_MAP).get()
                .addOnFailureListener { throw it }
                .addOnSuccessListener {
                    val coins: MutableSet<Coin> = mutableSetOf()
                    for (doc in it) {
                        val coin = doc.toObject(Coin::class.java)
                        coins.add(coin)
                    }
                    this.coins = coins
                    Prefs.mapLastUpdate = Utils.getToday()
                    callback()
                }
    }

    /**
     * Stores coins in an arbitrary collection, using a batch
     */
    private fun pushCoins(coins: Collection<Coin>, collection: CollectionReference, maybeBatch: WriteBatch?): Task<Void>? {
        val shouldCommit = maybeBatch == null
        val batch = maybeBatch ?: store().batch()

        for (coin in coins) {
            batch.set(collection.document(coin.id), coin)
        }

        if (shouldCommit) {
            return batch.commit()
        }
        return null
    }

    /**
     * Stores a new set of coins at the target date
     */
    private fun pushNewCoins(coins: Set<Coin>, date: Instant): Task<Void> {
        Timber.d("pushing coins")

        val batch = store().batch()

        // Update the last update field in the users/$id
        batch.update(getUserDocument(), "mapLastUpdate", Date.from(date))

        // Set our collection to users/$id/map, this is the map of coin objects
        val collection = getUserDocument().collection(COLLECTION_MAP)

        // Store each coin in that collection
        pushCoins(coins, collection, batch)

        return batch.commit()
    }

    /**
     * pushUserCoins stores coinsIn a specific user, deleting from the current user's account
     */
    fun pushUserCoins(uid: String, coinsToPush: Set<Coin>): Task<Void> {
        val batch = store().batch()
        val collection = getUserDocument(uid).collection(COLLECTION_IN)
        for (coin in coinsToPush) {
            // Withdraw from local account
            getAccount(coin.currency).withdraw(coin)

            // Delete from remote db
            getAccountCollection(coin.currency).document(coin.id).delete()

            // Store in target's coinsIn
            collection.document(coin.id).set(Coin(coin.id, coin.currency, coin.latLng, coin.value, true))
        }
        return batch.commit()
    }

    /**
     * Stores coins in their respective accounts
     */
    fun pushAccountCoins(coinsToPush: Collection<Coin>): Task<Void> {
        Timber.d("Moving coins into correct accounts")

        val grouping = coinsToPush.groupBy { it.currency }
        val batch = store().batch()
        for ((currency, theseCoins) in grouping) {
            val acc = accounts.find { it.currency == currency }
            acc?.deposit(*theseCoins.toTypedArray())

            val collection = getAccountCollection(currency)
            pushCoins(theseCoins, collection, batch)
        }

        return batch.commit()
    }

    /**
     * coinGoldValue returns the value of the coin in gold
     */
    private fun coinGoldValue(coin: Coin): Double = coin.value * when (coin.currency) {
        Currency.GOLD -> 1.0
        Currency.DOLR -> rates.DOLR
        Currency.PENY -> rates.PENY
        Currency.QUID -> rates.QUID
        Currency.SHIL -> rates.SHIL
    }

    /**
     * convertCoinsToGold takes coins as an argument.
     *
     * It withdraws them from their respective banks, and sends it to the gold account.
     */
    private fun convertCoinsToGold(coinsToMove: Collection<Coin>): Task<Void> {
        val batch = store().batch()
        var bufferCoinsBanked = coinsBankedToday
        for (coin in coinsToMove) {
            if (coin.currency == Currency.GOLD) {
                throw AssertionError("GOLD coin cannot be converted to gold")
            }

            // Withdraw coin from its account
            val fromAccount = getAccount(coin.currency)
            fromAccount.withdraw(coin)

            // Remove coin from the wallet db
            val fromCollection = getAccountCollection(coin.currency)
            batch.delete(fromCollection.document(coin.id))

            // Contribute coin to limit if need be
            if (!coin.shared) {
                if (arePaymentsEnabled(bufferCoinsBanked)) {
                    throw AssertionError("Trying to deposit coin over threshold. This should not happen")
                }
                bufferCoinsBanked += 1
            }

            // Convert coin to gold
            val newCoin = Coin(coin.id, Currency.GOLD, coin.latLng, coinGoldValue(coin), coin.shared)

            // Deposit coin to gold account
            goldAccount.deposit(newCoin)

            // Add coin to gold account db
            val toCollection = getAccountCollection(newCoin.currency)
            pushCoins(listOf(newCoin), toCollection, batch)
        }

        coinsBankedToday = bufferCoinsBanked

        return batch.commit()
    }

    /**
     * deposit25 deposits unshared coins up to the 25 coin limit
     */
    fun deposit25() {
        val allCoins = arrayListOf<Coin>()

        // Build allCoins
        for (account in accounts) {
            if (account.currency != Currency.GOLD) {
                for (coin in account.getCoins()) {
                    if (!coin.shared) {
                        allCoins.add(coin)
                    }
                }
            }
        }

        // Sort by descending order
        allCoins.sortByDescending { coinGoldValue(it) }

        // Convert the first n coins to gold, where n is the number of coins allowed to take
        val numCoinsToTake = min(allCoins.size, getCoinsUntilSpareChange())
        convertCoinsToGold(allCoins.take(numCoinsToTake))
    }

    /**
     * bankOne banks the largest coin in the account
     */
    fun bankOne(account: Account) {
        val coin = account.getCoins().maxBy {
            if (arePaymentsEnabled() && !it.shared) {
                // ignore unshareable coins if limit reached, this ensures
                // we only bank the largest shareable coin
                return@maxBy 0.0
            }
            it.value
        }

        coin?.also {
            // Short-circuit if we are not allowed to deposit this coin
            if (arePaymentsEnabled() && !coin.shared) {
                return
            }

            convertCoinsToGold(listOf(coin))
        }
    }

    /**
     * bankAll() banks all coins to the GOLD account (up to 25 + all shared coins)
     *
     * Essentially a.k.a depositAll().
     */
    fun bankAll() {
        val theseCoins = accounts.filter { it.currency != Currency.GOLD }.map { it.getCoins() }.reduce { a, b ->
            a.plus(b)
        }

        val sharedCoins = theseCoins.filter { it.shared }
        val largestCoins = theseCoins.filter { !it.shared }.sortedByDescending { coinGoldValue(it) }

        convertCoinsToGold(sharedCoins.plus(
                largestCoins.take(getCoinsUntilSpareChange())
        ))
    }

    /**
     * bankAll(account) banks all coins in the specified account to the GOLD account
     *
     * This is a variant of deposit25 and bankAll
     */
    fun bankAll(account: Account) {
        if (account.currency == Currency.GOLD) {
            throw AssertionError("can't bank gold account")
        }

        val sharedCoins = account.getCoins().filter { it.shared }
        val largestCoins = account.getCoins().filter { !it.shared }.sortedByDescending {
            coinGoldValue(it)
        }

        convertCoinsToGold(sharedCoins.plus(
                largestCoins.take(getCoinsUntilSpareChange())
        ))
    }

    /**
     * setupNewDay downloads and pushes new coins
     */
    private fun setupNewDay(callback: () -> Unit) {
        Timber.d("Grabbing latest JSON from server, and pushing to server")

        // Delete all coins in the collection
        getUserDocument().collection(COLLECTION_MAP).get()
                .addOnFailureListener { throw it }
                .addOnSuccessListener { result ->
                    val batch = store().batch()
                    for (doc in result) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().addOnFailureListener { throw it }

                    // Get latest JSON
                    DownloadFileTask(Utils.getMapURL()) { json ->
                        Timber.d("JSON downloaded")
                        val coins: MutableSet<Coin> = mutableSetOf()
                        val collection = FeatureCollection.fromJson(json)

                        // Create a rates object from the json
                        val ratesObj = JsonParser().parse(json).asJsonObject.getAsJsonObject("rates")
                        rates = Rates(
                                ratesObj.get("SHIL").asDouble,
                                ratesObj.get("DOLR").asDouble,
                                ratesObj.get("QUID").asDouble,
                                ratesObj.get("PENY").asDouble
                        )
                        Timber.d("Rates are: %s", rates.toString())

                        // Push these rates up
                        getUserDocument().update("rates", rates.toMap())


                        for (feature in collection.features()!!) {
                            val coord = feature.geometry()!! as Point

                            val currency = try {
                                Currency.valueOf(feature.getStringProperty("currency"))
                            } catch (_: IllegalArgumentException) {
                                // App may be out of date if this is an unknown currency
                                // "out of date" = schema has been updated
                                continue
                            }

                            val coin = Coin(
                                    feature.getStringProperty("id"),
                                    currency,
                                    LatLng(coord.latitude(), coord.longitude()),
                                    feature.getNumberProperty("value").toDouble(),
                                    false
                            )
                            coins.add(coin)
                        }

                        val today = Utils.getToday()
                        pushNewCoins(coins, today)
                                .addOnSuccessListener {
                                    this.coins = coins
                                    Prefs.mapLastUpdate = today
                                    Timber.d("Calling refresh's callback")
                                    callback()
                                }
                                .addOnFailureListener {
                                    throw it
                                }
                    }
                }
    }

    /**
     * shouldUpdate updates the local state and responds with whether or not mapLastUpdate needs changing
     */
    private fun shouldUpdate(callback: (Boolean) -> Unit) {
        // First check if the server is out of date
        getUserDocument().get()
                .addOnSuccessListener {
                    val serverDate = it.getDate("mapLastUpdate")?.toInstant() ?: Instant.EPOCH
                    coinsBankedToday = it.getDouble("coinsBankedToday")?.toInt() ?: 0

                    it.data?.get("rates")?.let {
                        rates = Rates.fromMap(it as Map<String,Any>)
                        Timber.d("Rates: %s", rates)
                    }

                    name = it.getString("name") ?: "Unknown Umbridge"
                    email = it.getString("email") ?: "unknown@umbrid.ge"

                    callback(serverDate.truncatedTo(ChronoUnit.DAYS) != Utils.getToday())
                }
                .addOnFailureListener { throw it }
    }

    /**
     * Gets all accounts based on the Currency enum
     */
    private fun fetchAccounts(callback: () -> Unit, increment: () -> Unit) {
        accounts = mutableListOf()
        for (currency in Currency.values()) {
            accounts.add(Account(Currency.GOLD, setOf()))
        }

        for (currency in Currency.values()) {
            // Increment the callback syncer so that callback is only called when all the data is ready
            increment()
            getAccountCollection(currency).get()
                    .addOnFailureListener { throw it }
                    .addOnSuccessListener {
                        val acc = Account(
                                currency,
                                it.toObjects(Coin::class.java).toSet()
                        )

                        accounts[currency.ordinal] = acc

                        if (currency == Currency.GOLD) {
                            goldAccount = acc
                        }

                        callback()
                    }

        }
    }

    /**
     * dropFriend unfriends a friend
     */
    fun dropFriend(friend: Friend): Task<Void> {
        friends.remove(friend)
        return getUserDocument().collection(COLLECTION_FRIENDS).document(friend.id).delete()
    }

    /**
     * addFriend adds a person as a friend
     */
    fun addFriend(uid: String) {
        getUserDocument(uid).get()
                .addOnFailureListener { throw it }
                .addOnSuccessListener {
                    val name = it.getString("name")!!
                    val email = it.getString("email")!!
                    val friend = Friend(uid, name, email)
                    friends.add(friend)
                    getUserDocument().collection(COLLECTION_FRIENDS).document(uid).set(friend)
                }
    }
    fun addFriend(friend: Friend): Task<Void> {
        friends.add(friend)
        return getUserDocument().collection(COLLECTION_FRIENDS).document(friend.id).set(friend)
    }

    /**
     * findFriend finds by an email, triggers a callback with the user doc
     */
    fun findFriend(email: String, callback: (Friend?) -> Unit) {
        store().collection("users").whereEqualTo("email", email).get()
                .addOnFailureListener { throw it }
                .addOnSuccessListener {
                    val found = it.documents.size != 0
                    if (found && email != this.email) {
                        val u = it.documents[0]
                        val friend = Friend(u.id, u.getString("name")!!, u.getString("email")!!)
                        callback(friend)
                    } else {
                        callback(null)
                    }
                }
    }

    fun refresh(callback: () -> Unit) {
        var syncer = 0
        val syncCallback = {
            syncer--
            if (syncer < 0) {
                throw RuntimeException("Syncer callback out of sync")
            } else if (syncer == 0) {
                callback()
            }
        }

        // This syncer must only be used in the current thread. Otherwise you risk race conditions.
        val incrementSyncer = {syncer++; Unit}

        accounts.clear()
        incrementSyncer()
        getUserDocument().collection(COLLECTION_IN).get()
                .addOnFailureListener { throw it }
                .addOnSuccessListener {
                    val coins = it.toObjects(Coin::class.java)

                    // Delete these coins from coinsIn
                    val batch = store().batch()
                    for (coin in coins) {
                        batch.delete(getUserDocument().collection(COLLECTION_IN).document(coin.id))
                    }
                    batch.commit()

                    pushAccountCoins(coins)
                            .addOnFailureListener { throw it }
                            .addOnSuccessListener {
                                syncCallback()
                                fetchAccounts(syncCallback, incrementSyncer)
                            }
                }

        // get friends
        incrementSyncer()
        getUserDocument().collection(COLLECTION_FRIENDS).get()
                .addOnFailureListener { throw it }
                .addOnSuccessListener {
                    this.friends = it.toObjects(Friend::class.java).toMutableSet()
                    syncCallback()
                }

        // for the setupNewDay/fetchCoins
        incrementSyncer()
        shouldUpdate { updateNeeded ->
            if (updateNeeded) {
                setupNewDay(syncCallback)
                coinsBankedToday = 0
            } else {
                fetchCoins(syncCallback)
            }
        }
    }
}