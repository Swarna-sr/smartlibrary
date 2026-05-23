package com.example.smartlibrary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class BookDetailActivity : AppCompatActivity() {

    private val firebaseDb = FirebaseDatabase.getInstance(
        FirebaseUtils.DATABASE_URL
    ).reference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        val titleText = findViewById<TextView>(R.id.detailTitle)
        val authorText = findViewById<TextView>(R.id.detailAuthor)
        val descriptionText = findViewById<TextView>(R.id.detailDescription)
        val imageView = findViewById<ImageView>(R.id.detailImage)

        val userRatingBar = findViewById<RatingBar>(R.id.userRatingBar)
        val reviewInput = findViewById<EditText>(R.id.reviewInput)

        val saveButton = findViewById<Button>(R.id.saveButton)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val shareBookButton = findViewById<Button>(R.id.shareBookButton)

        val googleRatingText = findViewById<TextView>(R.id.googleRatingText)
        val communityReviewsContainer = findViewById<LinearLayout>(R.id.communityReviewsContainer)

        val title = intent.getStringExtra("title") ?: "No Title"
        val author = intent.getStringExtra("author") ?: "Unknown Author"
        val description = intent.getStringExtra("description") ?: "No description available"
        val imageUrl = (intent.getStringExtra("imageUrl") ?: "")
            .replace("http://", "https://")

        val googleRating = intent.getDoubleExtra("googleRating", 0.0)
        val ratingsCount = intent.getIntExtra("ratingsCount", 0)

        titleText.text = title
        authorText.text = author
        descriptionText.text = description

        if (googleRating > 0.0) {
            googleRatingText.text = "Google Rating: $googleRating/5 ($ratingsCount ratings)"
        } else {
            googleRatingText.text = "Google Rating: Not available"
        }

        if (imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        shareBookButton.setOnClickListener {
            val shareIntent = Intent(this, ShareBookActivity::class.java)
            shareIntent.putExtra("title", title)
            shareIntent.putExtra("author", author)
            shareIntent.putExtra("description", description)
            shareIntent.putExtra("imageUrl", imageUrl)
            startActivity(shareIntent)
        }

        saveButton.setOnClickListener {
            val userRating = userRatingBar.rating
            val review = reviewInput.text.toString().trim()

            val favoriteBook = FavoriteBook(
                title = title,
                author = author,
                description = description,
                imageUrl = imageUrl,
                rating = userRating,
                review = review
            )

            lifecycleScope.launch {
                BookDatabase.getDatabase(this@BookDetailActivity)
                    .favoriteBookDao()
                    .insert(favoriteBook)

                saveCommunityReview(title, userRating, review)

                Toast.makeText(
                    this@BookDetailActivity,
                    "Book saved to My Library",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        deleteButton.setOnClickListener {
            lifecycleScope.launch {
                BookDatabase.getDatabase(this@BookDetailActivity)
                    .favoriteBookDao()
                    .deleteByTitle(title)

                Toast.makeText(
                    this@BookDetailActivity,
                    "Book removed from My Library",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        loadCommunityReviews(title, communityReviewsContainer)
    }

    private fun saveCommunityReview(bookTitle: String, rating: Float, reviewText: String) {
        if (reviewText.isEmpty()) {
            return
        }

        val profilePrefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val userName = profilePrefs.getString("userName", "Anonymous") ?: "Anonymous"

        val safeTitle = makeFirebaseSafeKey(bookTitle)

        val reviewData = CommunityReviews(
            userName = userName,
            rating = rating,
            review = reviewText
        )

        firebaseDb.child("community_reviews")
            .child(safeTitle)
            .push()
            .setValue(reviewData)
    }

    private fun loadCommunityReviews(bookTitle: String, container: LinearLayout) {
        val safeTitle = makeFirebaseSafeKey(bookTitle)

        firebaseDb.child("community_reviews")
            .child(safeTitle)
            .get()
            .addOnSuccessListener { snapshot ->
                container.removeAllViews()

                if (!snapshot.exists()) {
                    val noReviewText = TextView(this)
                    noReviewText.text = "No community reviews yet"
                    noReviewText.textSize = 14f
                    container.addView(noReviewText)
                    return@addOnSuccessListener
                }

                for (child in snapshot.children) {
                    val communityReview = child.getValue(CommunityReviews::class.java)

                    if (communityReview != null) {
                        val reviewView = TextView(this)
                        reviewView.text =
                            "${communityReview.userName}: ${communityReview.rating}/5\n${communityReview.review}"
                        reviewView.textSize = 14f
                        reviewView.setPadding(8, 8, 8, 8)

                        container.addView(reviewView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to load community reviews",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun makeFirebaseSafeKey(text: String): String {
        return text
            .replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace("/", "_")
    }
}