package jp.qais.coinz

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_payfriend_dialog.*
import java.lang.AssertionError

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    PayFriendDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [PayFriendDialogFragment.Listener].
 */
class PayFriendDialogFragment : BottomSheetDialogFragment() {
    private var mListener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payfriend_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnSend = view.findViewById<Button>(R.id.btnSend)
        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val group = view.findViewById<RadioGroup>(R.id.radio)

        spinner.isEnabled = false
        btnSend.isEnabled = false

        group.setOnCheckedChangeListener { group, checkedId ->
            val btn = group.findViewById<RadioButton>(checkedId)
            val curr = Currency.valueOf(btn.text.toString())

            spinner.isEnabled = true
            btnSend.isEnabled = true

            val arr = DataManager.getAccount(curr).getCoins().toTypedArray().sortedBy { it.value }

            ArrayAdapter(context, android.R.layout.simple_spinner_item, arr).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }
        }

        btnSend.setOnClickListener {
            mListener?.let { listener ->
                listener.onPayFriendClicked(
                        this@PayFriendDialogFragment.arguments!!.getParcelable("friend"),
                        spinner.selectedItem as Coin
                )
                dismiss()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        if (parent != null) {
            mListener = parent as Listener
        } else {
            mListener = context as Listener
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    interface Listener {
        fun onPayFriendClicked(friend: Friend, coin: Coin)
    }

    companion object {

        // TODO: Customize parameters
        fun newInstance(friend: Friend): PayFriendDialogFragment =
            PayFriendDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("friend", friend)
                }
            }

    }
}
