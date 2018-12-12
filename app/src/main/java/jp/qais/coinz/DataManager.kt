package jp.qais.coinz

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import timber.log.Timber
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
    lateinit var coins: ArrayList<Coin>

    private fun store() = FirebaseFirestore.getInstance()

    fun getUserID() = FirebaseAuth.getInstance().currentUser!!.uid
    fun getUserDocument() =
        FirebaseFirestore.getInstance().document("users/${getUserID()}")

    private fun fetchCoins(callback: () -> Unit) {

    }

    /**
     * Stores a new set of coins at the target date
     */
    private fun pushNewCoins(coins: ArrayList<Coin>, date: Instant): Task<Void> {
        Timber.d("pushing coins")

        val batch = store().batch()

        // Update the last update field in the users/$id
        batch.update(getUserDocument(), "mapLastUpdate", Date.from(date))

        // Set our collection to users/$id/map, this is the map of coin objects
        val collection = getUserDocument().collection("map")

        // Store each coin in that collection
        for (coin in coins) {
            batch.set(collection.document(coin.id), coin)
        }

        return batch.commit()
    }

    /**
     * Download and push new coins
     */
    private fun downloadAndPushNewCoins(callback: () -> Unit) {
        Timber.d("Grabbing latest JSON from server, and pushing to server")

        // Get latest JSON
        DownloadFileTask(Utils.getMapURL()) { json ->
            Timber.d("JSON downloaded")
            val coins = ArrayList<Coin>()
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
                        feature.getNumberProperty("value").toFloat()
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


    private fun shouldUpdate(callback: (Boolean) -> Unit) {
        // First check if the server is out of date
        getUserDocument().get()
                .addOnSuccessListener {
                    val serverDate = it.getDate("mapLastUpdate")?.toInstant() ?: Instant.EPOCH
                    callback(serverDate.truncatedTo(ChronoUnit.DAYS) != Utils.getToday())
                }
                .addOnFailureListener { throw it }
    }

    fun refresh(callback: () -> Unit) {

        shouldUpdate { updateNeeded ->
            if (updateNeeded) {
                downloadAndPushNewCoins(callback)
            } else {
                Timber.d("We ain't calling you yet.")
            }
        }
    }
}