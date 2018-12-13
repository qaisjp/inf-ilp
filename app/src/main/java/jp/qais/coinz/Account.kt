package jp.qais.coinz

enum class AccountType { BANK, WALLET }
data class Account(val type: AccountType, val balance: Float, val currency: Currency)
