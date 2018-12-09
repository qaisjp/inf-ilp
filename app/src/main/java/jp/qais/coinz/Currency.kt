package jp.qais.coinz

import android.content.Context

enum class Currency(val textID: Int, val colorID: Int) {
    GOLD(R.string.currency_gold, R.color.currency_gold),
    PENY(R.string.currency_peny, R.color.currency_peny),
    DOLR(R.string.currency_dolr, R.color.currency_dolr),
    SHIL(R.string.currency_shil, R.color.currency_shil),
    QUID(R.string.currency_quid, R.color.currency_quid);

    fun getString(ctx: Context) = ctx.getString(textID)
    fun getColor(ctx: Context) = ctx.getColor(colorID)
}
