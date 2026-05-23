package com.example.smartlibrary

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatTitleText: TextView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button

    private lateinit var database: FirebaseDatabase

    private var currentUserEmail: String = ""
    private var receiverEmail: String = ""
    private var chatId: String = ""

    private val messagesList = ArrayList<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatTitleText = findViewById(R.id.chatTitleText)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendMessageButton = findViewById(R.id.sendMessageButton)

        database = FirebaseDatabase.getInstance(FirebaseUtils.DATABASE_URL)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserEmail = prefs.getString("currentUserEmail", "") ?: ""

        receiverEmail = intent.getStringExtra("receiverEmail") ?: ""

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Current user missing. Please logout and login again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (receiverEmail.isEmpty()) {
            Toast.makeText(this, "Receiver missing. Please open chat again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        chatTitleText.text = "Chat with $receiverEmail"

        chatId = FirebaseUtils.getChatId(currentUserEmail, receiverEmail)

        chatAdapter = ChatAdapter(messagesList, currentUserEmail)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        sendMessageButton.setOnClickListener {
            sendMessage()
        }

        loadMessages()
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show()
            return
        }

        val message = ChatMessage(
            senderEmail = currentUserEmail,
            receiverEmail = receiverEmail,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )

        database.getReference("chats")
            .child(chatId)
            .push()
            .setValue(message)
            .addOnSuccessListener {
                messageEditText.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadMessages() {
        database.getReference("chats")
            .child(chatId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messagesList.clear()

                    for (child in snapshot.children) {
                        val message = child.getValue(ChatMessage::class.java)
                        if (message != null) {
                            messagesList.add(message)
                        }
                    }

                    chatAdapter.notifyDataSetChanged()

                    if (messagesList.isNotEmpty()) {
                        chatRecyclerView.scrollToPosition(messagesList.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Firebase error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}