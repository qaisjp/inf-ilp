package jp.qais.coinz

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
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
    private var accounts: MutableList<Account> = mutableListOf()
    private lateinit var goldAccount: Account

    const val SPARE_CHANGE_THRESHOLD = 25

    /**
     * Determines whether or not payments are enabled (with a backing field for optimisation)
     */
    private var _coinsBankedToday: Int = 0
    var coinsBankedToday: Int
        get() = _coinsBankedToday
        set(value) {
            // Don't do anything if it's already the correct value
            if (value == _coinsBankedToday) return

            Timber.d("coinsBankedToday now set to %s", value)
            getUserDocument().update("coinsBankedToday", value)
            _coinsBankedToday = value
        }

    fun arePaymentsEnabled(num: Int) = num >= SPARE_CHANGE_THRESHOLD
    fun arePaymentsEnabled() = arePaymentsEnabled(coinsBankedToday)
    fun getCoinsUntilSpareChange() = SPARE_CHANGE_THRESHOLD - coinsBankedToday

    private const val COLLECTION_MAP = "map"

    private fun store() = FirebaseFirestore.getInstance()

    fun getUserID() = FirebaseAuth.getInstance().currentUser!!.uid
    fun getUserDocument() = store().document("users/${getUserID()}")
    fun getAccountCollection(currency: Currency) = getUserDocument().collection("accounts-$currency")
    fun getAccount(currency: Currency) = accounts.first { it.currency == currency }

    /**
     * updateCoinsBankedCount updates our local state of coinsBankedToday
     */
    private fun updateCoinsBankedCount(): Task<DocumentSnapshot> {
        return getUserDocument().get()
                .addOnFailureListener { throw it }
                .addOnSuccessListener {
                    coinsBankedToday = it.getDouble("coinsBankedToday")?.toInt() ?: 0
                }
    }

    /**
     * Get coins as a non-mutable set
     */
    fun getCoins() = coins as Set<Coin>

    /**
     * Get accounts as a non-mutable list
     */
    fun getAccounts() = accounts as List<Account>

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
     * Stores coins in their respective accounts
     */
    fun pushAccountCoins(coinsToPush: Set<Coin>): Task<Void> {
        Timber.d("Moving coins into correct accounts")

        val grouping = coinsToPush.groupBy { it.currency }
        val batch = store().batch()
        for ((currency, theseCoins) in grouping) {
            val acc = accounts.find { it.currency == currency }!!
            acc.deposit(*theseCoins.toTypedArray())

            val collection = getAccountCollection(currency)
            pushCoins(theseCoins, collection, batch)
        }

        return batch.commit()
    }

    /**
     * convertCoinsToGold takes coins as an argument.
     *
     * It withdraws them from their respective banks, and sends it to the gold account.
     */
    private fun convertCoinsToGold(coinsToMove: Collection<Coin>) {
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
            val newCoin = Coin(coin.id, Currency.GOLD, coin.latLng, coin.value * 100, coin.shared) // todo: rates

            // Deposit coin to gold account
            goldAccount.deposit(newCoin)

            // Add coin to gold account db
            val toCollection = getAccountCollection(newCoin.currency)
            pushCoins(listOf(newCoin), toCollection, batch)
        }

        coinsBankedToday = bufferCoinsBanked

        // todo: data uplink
//        return batch.commit()
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
        allCoins.sortByDescending { c ->
            c.value // todo: rates
        }

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
                return@maxBy 0f
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
        val largestCoins = theseCoins.filter { !it.shared }.sortedByDescending {
            it.value // todo rates
        }

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

            it.value // todo: rates
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
                                    feature.getNumberProperty("value").toFloat(),
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

    private fun shouldUpdate(callback: (Boolean) -> Unit) {
        // First check if the server is out of date
        getUserDocument().get()
                .addOnSuccessListener {
                    val serverDate = it.getDate("mapLastUpdate")?.toInstant() ?: Instant.EPOCH
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

        if (accounts.isEmpty()) {
            fetchAccounts(syncCallback, incrementSyncer)
        }

        incrementSyncer()
        shouldUpdate { updateNeeded ->
            if (updateNeeded) {
                setupNewDay(syncCallback)
                coinsBankedToday = 0
            } else {
                fetchCoins(syncCallback)
                updateCoinsBankedCount()
            }
        }
    }
}