package jp.qais.coinz

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_payments, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = FriendsViewAdapter(listOf(
                Friend(DataManager.getUserID(), "Qais Patankar", "qaisjp@gmail.com"),
                Friend("someuuid", "Ally White", "ally@hvitt.co.uk")
        ))

        recyclerView = view.findViewById<RecyclerView>(R.id.friendsRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return view
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

    }
}
