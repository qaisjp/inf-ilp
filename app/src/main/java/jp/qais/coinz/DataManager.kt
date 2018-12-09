package jp.qais.coinz

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import java.text.SimpleDateFormat
import java.time.Instant
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

    private fun pushNewCoins(coins: ArrayList<Coin>, date: Instant): Task<Void> {
        val batch = store().batch()

        // Update the last update field in the users/$id
        batch.update(getUserDocument(), "mapLastUpdate", Date.from(date))

        // Set our collection to users/$id/coins-yyyy-M-dd
        val subdir = SimpleDateFormat("yyyy-M-dd").format(Date.from(date))
        val collection = getUserDocument().collection("coins-$subdir")

        // Store each coin in that collection
        for (coin in coins) {
            batch.set(collection.document(coin.id), coin)
        }

        return batch.commit().also {
            it.addOnSuccessListener {
                this.coins = coins
            }
        }
    }

    fun refresh(callback: () -> Unit) {
        val today = Utils.getToday()
        if (Prefs.mapLastUpdate != today) {

            // Get latest JSON
            DownloadFileTask(Utils.getMapURL()) { json ->
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

                pushNewCoins(coins, today)
                        .addOnSuccessListener {
                            this.coins = coins
                            Prefs.mapLastUpdate = today
                            callback()
                        }
                        .addOnFailureListener {
                            throw it
                        }
            }

        }
    }
}