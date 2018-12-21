package jp.qais.coinz

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

/**
 * Displays a list of accounts
 */
class AccountViewAdapter(private val accounts: List<Account>) : RecyclerView.Adapter<AccountViewAdapter.AccountViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class AccountViewHolder(val view: CardView) : RecyclerView.ViewHolder(view) {
        val balance = view.findViewById<TextView>(R.id.text_balance)!!
        val balanceDescription = view.findViewById<TextView>(R.id.text_balance_description)!!
        val currency = view.findViewById<TextView>(R.id.text_currency)!!

        val bankOne = view.findViewById<Button>(R.id.bankOne)!!
        val bankAll = view.findViewById<Button>(R.id.bankAll)!!
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AccountViewHolder {
        // create a new view
        val cardView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_account, parent, false) as CardView

        return AccountViewHolder(cardView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val account = accounts[position]
        val context = holder.view.context
        val discreteCoins = account.getCoins().size
        val sharedCoins = account.getCoins().sumBy { if (it.shared) 1 else 0 }
        val isGold = account.currency == Currency.GOLD
        val remainingBankableCoins = DataManager.getCoinsUntilSpareChange()

        holder.bankOne.text = when (isGold) {
            true -> context.getText(R.string.deposit_25)
            false -> context.getText(R.string.bank_largest)
        }

        holder.bankAll.text = when (isGold) {
            true -> context.getText(R.string.deposit_all)
            false -> context.getText(R.string.bank_all)
        }

        holder.bankOne.setOnClickListener {
            if (isGold) {
                DataManager.deposit25()
            } else {
                DataManager.bankOne(account)
            }
            notifyDataSetChanged()
        }

        holder.bankAll.setOnClickListener {
            if (isGold) {
                DataManager.bankAll()
            } else {
                DataManager.bankAll(account)
            }
            notifyDataSetChanged()
        }

        holder.balance.text = String.format("%.05f", account.getBalance())
        holder.balanceDescription.text = when (isGold) {
            true -> context.resources.getQuantityString(R.plurals.in_bank, remainingBankableCoins, remainingBankableCoins)
            false -> context.resources.getQuantityString(R.plurals.in_wallet, discreteCoins, discreteCoins, sharedCoins)
        }

        holder.currency.text = account.currency.getString(context)

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = accounts.size
}