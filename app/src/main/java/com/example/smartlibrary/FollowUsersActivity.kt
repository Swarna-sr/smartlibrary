package com.example.smartlibrary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class FollowUsersActivity : AppCompatActivity() {

    private lateinit var searchUserEmail: EditText
    private lateinit var searchUserButton: Button
    private lateinit var followButton: Button
    private lateinit var searchResultText: TextView
    private lateinit var followingRecyclerView: RecyclerView

    private lateinit var database: FirebaseDatabase

    private var currentUserEmail: String = ""
    private var foundUserEmail: String = ""

    private val followingList = ArrayList<String>()
    private lateinit var followingAdapter: FollowingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_users)

        searchUserEmail = findViewById(R.id.searchUserEmail)
        searchUserButton = findViewById(R.id.searchUserButton)
        followButton = findViewById(R.id.followButton)
        searchResultText = findViewById(R.id.searchResultText)
        followingRecyclerView = findViewById(R.id.followingRecyclerView)

        database = FirebaseDatabase.getInstance(FirebaseUtils.DATABASE_URL)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserEmail = prefs.getString("currentUserEmail", "") ?: ""

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, "Logged in as $currentUserEmail", Toast.LENGTH_SHORT).show()

        followingAdapter = FollowingAdapter(
            followingList,
            onChatClick = { selectedEmail ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiverEmail", selectedEmail)
                startActivity(intent)
            },
            onUnfollowClick = { selectedEmail ->
                unfollowUser(selectedEmail)
            }
        )

        followingRecyclerView.layoutManager = LinearLayoutManager(this)
        followingRecyclerView.adapter = followingAdapter

        searchUserButton.setOnClickListener {
            val email = searchUserEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter user email", Toast.LENGTH_SHORT).show()
            } else if (email == currentUserEmail) {
                Toast.makeText(this, "You cannot follow yourself", Toast.LENGTH_SHORT).show()
            } else {
                searchUser(email)
            }
        }

        followButton.setOnClickListener {
            if (foundUserEmail.isEmpty()) {
                Toast.makeText(this, "Search user first", Toast.LENGTH_SHORT).show()
            } else {
                followUser(foundUserEmail)
            }
        }

        loadFollowingUsers()
    }

    private fun searchUser(email: String) {
        val userKey = FirebaseUtils.emailToKey(email)

        database.getReference("users")
            .child(userKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        foundUserEmail = email
                        searchResultText.text = "User found: $email"
                        followButton.visibility = View.VISIBLE
                    } else {
                        foundUserEmail = ""
                        searchResultText.text = "No user found with this email"
                        followButton.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@FollowUsersActivity,
                        "Firebase error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun followUser(emailToFollow: String) {
        val currentUserKey = FirebaseUtils.emailToKey(currentUserEmail)
        val followUserKey = FirebaseUtils.emailToKey(emailToFollow)

        val updates = HashMap<String, Any>()
        updates["following/$currentUserKey/$followUserKey"] = emailToFollow
        updates["followers/$followUserKey/$currentUserKey"] = currentUserEmail

        database.reference.updateChildren(updates)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Now following $emailToFollow",
                    Toast.LENGTH_SHORT
                ).show()

                searchResultText.text = "You are following $emailToFollow"
                followButton.visibility = View.GONE

                if (!followingList.contains(emailToFollow)) {
                    followingList.add(emailToFollow)
                    followingAdapter.notifyItemInserted(followingList.size - 1)
                }

                Log.d("FOLLOW_DEBUG", "Follow saved successfully")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Follow failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                Log.e("FOLLOW_DEBUG", "Follow failed", e)
            }
    }

    private fun unfollowUser(emailToUnfollow: String) {
        val currentUserKey = FirebaseUtils.emailToKey(currentUserEmail)
        val unfollowUserKey = FirebaseUtils.emailToKey(emailToUnfollow)

        val updates = HashMap<String, Any?>()

        updates["following/$currentUserKey/$unfollowUserKey"] = null
        updates["followers/$unfollowUserKey/$currentUserKey"] = null

        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Unfollowed $emailToUnfollow",
                    Toast.LENGTH_SHORT
                ).show()

                followingList.remove(emailToUnfollow)
                followingAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to unfollow: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadFollowingUsers() {
        val currentUserKey = FirebaseUtils.emailToKey(currentUserEmail)

        database.getReference("following")
            .child(currentUserKey)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    followingList.clear()

                    for (child in snapshot.children) {
                        val email = child.getValue(String::class.java)

                        if (!email.isNullOrEmpty()) {
                            followingList.add(email)
                        }
                    }

                    followingAdapter.notifyDataSetChanged()

                    Log.d("FOLLOW_DEBUG", "Following count: ${followingList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@FollowUsersActivity,
                        "Firebase error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}