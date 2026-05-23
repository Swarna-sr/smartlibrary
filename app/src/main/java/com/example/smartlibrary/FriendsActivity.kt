package com.example.smartlibrary

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class FriendsActivity : AppCompatActivity() {

    lateinit var container: LinearLayout
    lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        container = findViewById(R.id.friendsContainer)

        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val userName = prefs.getString("userName", "Anonymous")!!

        db = FirebaseDatabase.getInstance().reference

        db.child("users").child(userName).child("friends")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    container.removeAllViews()

                    for (friendSnap in snapshot.children) {
                        val friendName = friendSnap.key ?: continue

                        val btn = Button(this@FriendsActivity)
                        btn.text = "👤 $friendName"

                        btn.setOnClickListener {
                            val intent = Intent(this@FriendsActivity, ChatActivity::class.java)
                            intent.putExtra("receiver", friendName)
                            startActivity(intent)
                        }

                        container.addView(btn)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}