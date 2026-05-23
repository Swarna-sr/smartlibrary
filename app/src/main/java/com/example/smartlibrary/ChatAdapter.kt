package com.example.smartlibrary

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserEmail: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val senderText: TextView = itemView.findViewById(R.id.senderText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = messages[position]

        holder.messageText.text = chatMessage.message
        holder.senderText.text = chatMessage.senderEmail

        if (chatMessage.senderEmail == currentUserEmail) {
            holder.messageContainer.gravity = Gravity.END
            holder.messageText.text = "You: ${chatMessage.message}"
        } else {
            holder.messageContainer.gravity = Gravity.START
            holder.messageText.text = chatMessage.message
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}