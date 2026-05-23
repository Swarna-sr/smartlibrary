package com.example.smartlibrary

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShareBookActivity : AppCompatActivity() {

    private lateinit var bookToShareText: TextView
    private lateinit var shareUsersRecyclerView: RecyclerView

    private val database = FirebaseDatabase.getInstance(
        FirebaseUtils.DATABASE_URL
    )

    private var currentUserEmail: String = ""

    private var bookTitle: String = ""
    private var bookAuthor: String = ""
    private var bookDescription: String = ""
    private var bookImageUrl: String = ""

    private val followingList = ArrayList<String>()
    private lateinit var followingAdapter: FollowingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_book)

        bookToShareText = findViewById(R.id.bookToShareText)
        shareUsersRecyclerView = findViewById(R.id.shareUsersRecyclerView)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserEmail = prefs.getString("currentUserEmail", "") ?: ""

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bookTitle = intent.getStringExtra("title") ?: ""
        bookAuthor = intent.getStringExtra("author") ?: ""
        bookDescription = intent.getStringExtra("description") ?: ""
        bookImageUrl = intent.getStringExtra("imageUrl") ?: ""

        if (bookTitle.isEmpty()) {
            Toast.makeText(this, "Book details missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bookToShareText.text = "Sharing: $bookTitle"

        followingAdapter = FollowingAdapter(
            followingList,
            onChatClick = { selectedUserEmail ->
                shareBookWithUser(selectedUserEmail)
            },
            onUnfollowClick = { selectedUserEmail ->
                Toast.makeText(
                    this,
                    "Unfollow from Follow Users screen",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        shareUsersRecyclerView.layoutManager = LinearLayoutManager(this)
        shareUsersRecyclerView.adapter = followingAdapter

        loadFollowingUsers()
    }

    private fun loadFollowingUsers() {
        val currentUserKey = FirebaseUtils.emailToKey(currentUserEmail)

        database.getReference("following")
            .child(currentUserKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followingList.clear()

                    for (child in snapshot.children) {
                        val email = child.getValue(String::class.java)

                        if (!email.isNullOrEmpty()) {
                            followingList.add(email)
                        }
                    }

                    followingAdapter.notifyDataSetChanged()

                    if (followingList.isEmpty()) {
                        Toast.makeText(
                            this@ShareBookActivity,
                            "You are not following anyone yet",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ShareBookActivity,
                        "Firebase error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun shareBookWithUser(receiverEmail: String) {
        val receiverKey = FirebaseUtils.emailToKey(receiverEmail)

        val sharedBook = SharedBook(
            title = bookTitle,
            author = bookAuthor,
            description = bookDescription,
            imageUrl = bookImageUrl,
            senderEmail = currentUserEmail,
            receiverEmail = receiverEmail,
            timestamp = System.currentTimeMillis()
        )

        database.getReference("shared_books")
            .child(receiverKey)
            .push()
            .setValue(sharedBook)
            .addOnSuccessListener {

                // Also send shared book as a chat message
                sendBookToChat(receiverEmail)

                Toast.makeText(
                    this,
                    "Book shared with $receiverEmail",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to share: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun sendBookToChat(receiverEmail: String) {
        val chatId = FirebaseUtils.getChatId(currentUserEmail, receiverEmail)

        val message = ChatMessage(
            senderEmail = currentUserEmail,
            receiverEmail = receiverEmail,
            message = "📚 Shared a book:\n\nTitle: $bookTitle\nAuthor: $bookAuthor\n\nTap Shared Books to view details.",
            timestamp = System.currentTimeMillis()
        )

        database.getReference("chats")
            .child(chatId)
            .push()
            .setValue(message)
    }
}