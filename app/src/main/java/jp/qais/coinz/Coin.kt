package jp.qais.coinz

import android.os.Parcel
import android.os.Parcelable
import com.mapbox.mapboxsdk.geometry.LatLng

data class Coin(val id: String, val currency: Currency, val latLng: LatLng, val value: Float) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readParcelable(Currency::class.java.classLoader),
            parcel.readParcelable(LatLng::class.java.classLoader),
            parcel.readFloat())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeParcelable(currency, flags)
        parcel.writeParcelable(latLng, flags)
        parcel.writeFloat(value)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Coin> {
        override fun createFromParcel(parcel: Parcel) = Coin(parcel)
        override fun newArray(size: Int): Array<Coin?> = arrayOfNulls(size)
    }
}