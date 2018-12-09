package jp.qais.coinz

import com.mapbox.mapboxsdk.geometry.LatLng

data class Coin(val id: String, val currency: Currency, val latLng: LatLng, val value: Float) {
    fun toMap(): Map<String, Any> = HashMap<String, Any>().apply {
        this["id"] = id
        this["currency"] = currency.toString()
        this["lat"] = latLng.latitude
        this["lng"] = latLng.longitude
        this["value"] = value
    }
}