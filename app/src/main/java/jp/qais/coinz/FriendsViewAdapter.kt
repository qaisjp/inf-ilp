package jp.qais.coinz

import android.support.v4.app.FragmentManager
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso

class FriendsViewAdapter(private val friends: List<Friend>, private val childFragmentManager: FragmentManager) : RecyclerView.Adapter<FriendsViewAdapter.AccountViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class AccountViewHolder(val view: CardView) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imageView)!!
        val fullname = view.findViewById<TextView>(R.id.fullname)!!
        val description = view.findViewById<TextView>(R.id.description)!!
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AccountViewHolder {
        // create a new view
        val cardView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_person, parent, false) as CardView

        return AccountViewHolder(cardView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val friend = friends[position]
        val context = holder.view.context
        val isMe = friend.id == DataManager.getUserID()

        holder.fullname.text = friend.name

        Picasso.with(context)
                .load(friend.getGravatar())
                .placeholder(R.drawable.ic_tag_faces_black_24dp)
                .error(R.drawable.ic_tag_faces_black_24dp)
                .into(holder.imageView)

        // Suppress touches if it's me
        holder.view.setOnTouchListener { v, event -> isMe }
        holder.view.setOnClickListener {
            Toast.makeText(context, "tada", Toast.LENGTH_SHORT).show()
            val dialog = PayFriendDialogFragment.newInstance(friend)
            dialog.showNow(childFragmentManager, "account_settings")
        }

        if (isMe) {
            if (itemCount == 1) {
                holder.description.text = context.getString(R.string.prompt_add_friends)
            } else {
                holder.description.text = context.getString(R.string.prompt_select_friend)
            }
        } else {
            holder.description.text = context.getString(R.string.send_person_money, friend.email)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = friends.size
}