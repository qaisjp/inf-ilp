package jp.qais.coinz

class Account(var currency: Currency, initialCoins: Set<Coin>) {
    private var coins: MutableSet<Coin> = initialCoins.toMutableSet()

    fun getBalance() : Float {
        var balance = 0f
        for (coin in coins) {
            balance += coin.value
        }

        return balance
    }

    fun deposit(vararg newCoins: Coin) {
        coins.addAll(newCoins)
    }

    fun withdraw(vararg theseCoins: Coin) {
        coins.removeAll(theseCoins)
    }

    fun getCoins() = coins.toSet()
}
