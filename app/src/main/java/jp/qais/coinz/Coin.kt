package jp.qais.coinz

import com.mapbox.mapboxsdk.geometry.LatLng

class Coin(val id: String, val currency: Currency, val latLng: LatLng, val value: Float) {
}