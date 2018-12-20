package jp.qais.coinz

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.fragment_payments.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 *
 */
class PaymentsFragment : Fragment() {

    var doney = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payments, container, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.action_search) {
            return false
        }

        Toast.makeText(requireContext(), "Search", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun isEnabled() = DataManager.arePaymentsEnabled()

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        menu?.let {
            inflater?.inflate(R.menu.menu_payments, it)
            it.findItem(R.id.action_search).setEnabled(isEnabled())
        }
    }

    override fun onStart() {
        super.onStart()

        paymentsPaywall.visibility = if (isEnabled()) View.GONE else View.VISIBLE

        doney = true

    }
}
