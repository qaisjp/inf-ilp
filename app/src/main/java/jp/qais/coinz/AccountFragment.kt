package jp.qais.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
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
