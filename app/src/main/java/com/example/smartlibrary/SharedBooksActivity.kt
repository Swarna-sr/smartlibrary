package com.example.smartlibrary

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class SharedBooksActivity : AppCompatActivity() {

    private lateinit var sharedBooksContainer: LinearLayout

    private val database = FirebaseDatabase.getInstance(
        FirebaseUtils.DATABASE_URL
    )

    private var currentUserEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createLayout()

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserEmail = prefs.getString("currentUserEmail", "") ?: ""

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadSharedBooks()
    }

    private fun createLayout() {
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(Color.parseColor("#F6F3FF"))

        val mainContainer = LinearLayout(this)
        mainContainer.orientation = LinearLayout.VERTICAL
        mainContainer.setPadding(24, 32, 24, 32)

        val titleText = TextView(this)
        titleText.text = "📚 Shared Books"
        titleText.textSize = 26f
        titleText.setTextColor(Color.parseColor("#3F2E56"))
        titleText.gravity = Gravity.CENTER
        titleText.setTypeface(null, Typeface.BOLD)
        titleText.setPadding(0, 0, 0, 8)

        mainContainer.addView(titleText)

        val subtitleText = TextView(this)
        subtitleText.text = "Books shared with you by friends"
        subtitleText.textSize = 15f
        subtitleText.setTextColor(Color.parseColor("#555555"))
        subtitleText.gravity = Gravity.CENTER
        subtitleText.setPadding(0, 0, 0, 24)

        mainContainer.addView(subtitleText)

        sharedBooksContainer = LinearLayout(this)
        sharedBooksContainer.orientation = LinearLayout.VERTICAL

        mainContainer.addView(sharedBooksContainer)

        scrollView.addView(mainContainer)
        setContentView(scrollView)
    }

    private fun loadSharedBooks() {
        val currentUserKey = FirebaseUtils.emailToKey(currentUserEmail)

        database.getReference("shared_books")
            .child(currentUserKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sharedBooksContainer.removeAllViews()

                    if (!snapshot.exists()) {
                        showEmptyMessage()
                        return
                    }

                    var count = 0

                    for (child in snapshot.children) {
                        val sharedBook = child.getValue(SharedBook::class.java)

                        if (sharedBook != null) {
                            count++
                            addSharedBookCard(sharedBook)
                        }
                    }

                    if (count == 0) {
                        showEmptyMessage()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SharedBooksActivity,
                        "Firebase error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun showEmptyMessage() {
        val emptyText = TextView(this)
        emptyText.text = "No books have been shared with you yet."
        emptyText.textSize = 16f
        emptyText.setTextColor(Color.parseColor("#666666"))
        emptyText.gravity = Gravity.CENTER
        emptyText.setPadding(0, 40, 0, 0)

        sharedBooksContainer.addView(emptyText)
    }

    private fun addSharedBookCard(sharedBook: SharedBook) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.WHITE)
        card.setPadding(20, 20, 20, 20)

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 20)
        card.layoutParams = cardParams

        val imageView = ImageView(this)
        val imageParams = LinearLayout.LayoutParams(180, 240)
        imageParams.gravity = Gravity.CENTER
        imageView.layoutParams = imageParams
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setBackgroundColor(Color.parseColor("#E5E7EB"))

        if (sharedBook.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(sharedBook.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        card.addView(imageView)

        val titleText = TextView(this)
        titleText.text = sharedBook.title
        titleText.textSize = 20f
        titleText.setTextColor(Color.parseColor("#111827"))
        titleText.setTypeface(null, Typeface.BOLD)
        titleText.gravity = Gravity.CENTER
        titleText.setPadding(0, 14, 0, 4)

        card.addView(titleText)

        val authorText = TextView(this)
        authorText.text = "By ${sharedBook.author}"
        authorText.textSize = 15f
        authorText.setTextColor(Color.parseColor("#4B5563"))
        authorText.gravity = Gravity.CENTER
        authorText.setPadding(0, 0, 0, 8)

        card.addView(authorText)

        val senderText = TextView(this)
        senderText.text = "Shared by: ${sharedBook.senderEmail}"
        senderText.textSize = 13f
        senderText.setTextColor(Color.parseColor("#7C3AED"))
        senderText.gravity = Gravity.CENTER
        senderText.setPadding(0, 0, 0, 12)

        card.addView(senderText)

        val descriptionText = TextView(this)
        descriptionText.text = if (sharedBook.description.isNotEmpty()) {
            sharedBook.description
        } else {
            "No description available"
        }
        descriptionText.textSize = 14f
        descriptionText.setTextColor(Color.parseColor("#374151"))
        descriptionText.setPadding(0, 8, 0, 12)

        card.addView(descriptionText)

        val openButton = Button(this)
        openButton.text = "Open Book"
        openButton.isAllCaps = false
        openButton.setTextColor(Color.WHITE)
        openButton.setBackgroundColor(Color.parseColor("#3B82F6"))

        openButton.setOnClickListener {
            val intent = Intent(this, BookDetailActivity::class.java)
            intent.putExtra("title", sharedBook.title)
            intent.putExtra("author", sharedBook.author)
            intent.putExtra("description", sharedBook.description)
            intent.putExtra("imageUrl", sharedBook.imageUrl)
            startActivity(intent)
        }

        card.addView(openButton)

        sharedBooksContainer.addView(card)
    }
}