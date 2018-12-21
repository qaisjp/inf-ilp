package jp.qais.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_payments.*

/**
 * Fragment for displaying list of friends and ui for adding friends.
 *
 */
class PaymentsFragment : Fragment(), PayFriendDialogFragment.Listener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var emailEdit: EditText
    private lateinit var addFriendBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_payments, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = FriendsViewAdapter(childFragmentManager)

        recyclerView = view.findViewById<RecyclerView>(R.id.friendsRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        emailEdit = view.findViewById(R.id.emailEdit)
        addFriendBtn = view.findViewById(R.id.addFriendBtn)

        addFriendBtn.setOnClickListener {
            DataManager.findFriend(emailEdit.text.toString()) { friend ->
                if (friend == null) {
                    Toast.makeText(context, "That person does not exist.", Toast.LENGTH_LONG).show()
                    return@findFriend
                }

                emailEdit.setText("")
                DataManager.addFriend(friend)
                viewAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    override fun onPayFriendClicked(friend: Friend, coin: Coin) {
        DataManager.pushUserCoins(friend.id, setOf(coin))
        Toast.makeText(context, "${friend.name} (${friend.email}) has been sent $coin", Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId != R.id.action_add_friend) {
//            return false
//        }
//
//        Toast.makeText(requireContext(), getText(R.string.action_add_friend), Toast.LENGTH_SHORT).show()
        return true
    }

    private fun isEnabled() = DataManager.arePaymentsEnabled()

//    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
//        super.onCreateOptionsMenu(menu, inflater)
//
//        menu?.let {
//            inflater?.inflate(R.menu.menu_payments, it)
//            it.findItem(R.id.action_add_friend).setEnabled(isEnabled())
//        }
//    }

    override fun onStart() {
        super.onStart()

        paymentsPaywall.visibility = if (isEnabled()) View.GONE else View.VISIBLE

    }
}
