package jp.qais.coinz

import android.os.Parcel
import android.os.Parcelable

/**
 * Represents your friend to send coins to.
 */
data class Friend(val id: String, val name: String, val email: String) : Parcelable {
    // Required for Firestore creation of Friends
    constructor() : this("null", "Unknown", "unknown")

    companion object CREATOR : Parcelable.Creator<Friend> {
        override fun createFromParcel(parcel: Parcel) = Friend(parcel)
        override fun newArray(size: Int): Array<Friend?> = arrayOfNulls(size)
        val md5 = java.security.MessageDigest.getInstance("MD5")
    }

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    /**
     * Get their Gravatar in URL form (as a String)
     */
    // From: https://en.gravatar.com/site/implement/hash/ (inspiration)
    // From: https://stackoverflow.com/questions/4846484/md5-hashing-in-android
    fun getGravatar() : String {
        md5.let {
            it.update(email.trim().toLowerCase().toByteArray())
            return "https://www.gravatar.com/avatar/${Utils.toHexString(it.digest())}?s=128"
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
    }

    override fun describeContents() = 0
}