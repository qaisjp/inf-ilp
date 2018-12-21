package jp.qais.coinz

/**
 * Represents a bank account or wallet account.
 */
class Account(var currency: Currency, initialCoins: Set<Coin>) {
    private var coins: MutableSet<Coin> = initialCoins.toMutableSet()

    /**
     * Returns to the total value of all cois in this account
     */
    fun getBalance() : Double {
        var balance = 0.0
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
