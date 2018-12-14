package jp.qais.coinz

data class Account(val currency: Currency, val coins: Set<Coin>) {
    fun getBalance() : Float {
        var balance = 0f
        for (coin in coins) {
            balance += coin.value
        }
        return balance
    }
}
