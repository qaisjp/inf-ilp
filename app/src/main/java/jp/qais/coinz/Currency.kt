package jp.qais.coinz

import android.content.Context

enum class Currency(val textID: Int) {
    GOLD(R.string.currency_gold),
    PENY(R.string.currency_peny),
    DOLR(R.string.currency_dolr),
    SHIL(R.string.currency_shil),
    QUID(R.string.currency_quid);

    fun getString(ctx: Context): String = ctx.getString(textID)
}
