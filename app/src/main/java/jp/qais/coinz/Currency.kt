package jp.qais.coinz

import android.content.Context
import android.os.Parcel
import android.os.Parcelable

enum class Currency(val textID: Int, val colorID: Int) : Parcelable {
    GOLD(R.string.currency_gold, R.color.currency_gold),
    PENY(R.string.currency_peny, R.color.currency_peny),
    DOLR(R.string.currency_dolr, R.color.currency_dolr),
    SHIL(R.string.currency_shil, R.color.currency_shil),
    QUID(R.string.currency_quid, R.color.currency_quid);

    fun getString(ctx: Context) = ctx.getString(textID)
    fun getColor(ctx: Context) = ctx.getColor(colorID)

    override fun writeToParcel(parcel: Parcel, flags: Int) = parcel.writeString(toString())
    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Currency> {
        override fun createFromParcel(parcel: Parcel) = Currency.valueOf(parcel.readString())
        override fun newArray(size: Int): Array<Currency?> = arrayOfNulls(size)
    }
}
