package jp.qais.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

/**
 * A simple [Fragment] subclass.
 *
 */
class AccountFragment : Fragment(), SettingsDialogFragment.Listener {
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
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = AccountViewAdapter(DataManager.getAccounts())

        recyclerView = view.findViewById<RecyclerView>(R.id.accountRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.action_settings) {
            return false
        }

        Toast.makeText(requireContext(), "Settings", Toast.LENGTH_SHORT).show()

        val settings = SettingsDialogFragment.newInstance(5)
        settings.showNow(childFragmentManager, "account_settings")

        return true
    }

    override fun onSettingClicked(position: Int) {
        Toast.makeText(requireContext(), String.format("Setting %d clicked", position), Toast.LENGTH_SHORT).show()
    }
}
