package com.example.smartlibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FollowingAdapter(
    private val users: List<String>,
    private val onChatClick: (String) -> Unit,
    private val onUnfollowClick: (String) -> Unit
) : RecyclerView.Adapter<FollowingAdapter.FollowingViewHolder>() {

    class FollowingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailText: TextView = itemView.findViewById(R.id.followingEmailText)
        val chatButton: Button = itemView.findViewById(R.id.chatUserButton)
        val unfollowButton: Button = itemView.findViewById(R.id.unfollowUserButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_following, parent, false)

        return FollowingViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val email = users[position]

        holder.emailText.text = email

        holder.chatButton.setOnClickListener {
            onChatClick(email)
        }

        holder.unfollowButton.setOnClickListener {
            onUnfollowClick(email)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}