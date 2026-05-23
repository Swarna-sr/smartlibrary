package com.example.smartlibrary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch
import android.view.View
import android.text.Editable
import android.text.TextWatcher

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var searchButton: Button
    private lateinit var favoritesButton: Button
    private lateinit var followUsersButton: Button
    private lateinit var sharedBooksButton: Button
    private lateinit var suggestionsButton: Button
    private lateinit var logoutButton: Button

    private var isSearching = false
    private var lastSearchTime = 0L
    private val searchCache = HashMap<String, List<Item>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)

        recyclerView = findViewById(R.id.recyclerView)
        searchBar = findViewById(R.id.searchBar)
        searchButton = findViewById(R.id.searchButton)
        favoritesButton = findViewById(R.id.favoritesButton)
        followUsersButton = findViewById(R.id.followUsersButton)
        sharedBooksButton = findViewById(R.id.sharedBooksButton)
        suggestionsButton = findViewById(R.id.suggestionsButton)
        logoutButton = findViewById(R.id.logoutButton)

        recyclerView.layoutManager = LinearLayoutManager(this)

        profileIcon.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim().lowercase()
            val currentTime = System.currentTimeMillis()

            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter a book name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentTime - lastSearchTime < 8000) {
                Toast.makeText(this, "Please wait 8 seconds before searching again", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lastSearchTime = currentTime
            searchBooks(query)
        }
        searchBar.setOnClickListener {
            if (searchBar.text.toString().trim().isEmpty()) {
                showDashboardButtons()
            }
        }

        favoritesButton.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        followUsersButton.setOnClickListener {
            startActivity(Intent(this, FollowUsersActivity::class.java))
        }

        sharedBooksButton.setOnClickListener {
            startActivity(Intent(this, SharedBooksActivity::class.java))
        }

        suggestionsButton.setOnClickListener {
            startActivity(Intent(this, SuggestionsActivity::class.java))
        }

        logoutButton.setOnClickListener {
            val loginPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

            loginPrefs.edit()
                .putBoolean("isLoggedIn", false)
                .remove("currentUserEmail")
                .apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isEmpty()) {
                    recyclerView.adapter = null
                    showDashboardButtons()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchBooks(query: String) {
        if (isSearching) return

        if (searchCache.containsKey(query)) {
            val cachedBooks = searchCache[query] ?: emptyList()
            recyclerView.adapter = BookAdapter(cachedBooks)
            Toast.makeText(this, "Loaded from saved search results", Toast.LENGTH_SHORT).show()
            return
        }

        isSearching = true
        searchButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.searchBooks(query)
                val books = response.items

                Log.d("BOOK_API", "Search: $query, Items: ${books.size}")

                if (books.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "No books found. Try another search.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    searchCache[query] = books
                }

                recyclerView.adapter = BookAdapter(books)
                hideDashboardButtons()

            } catch (e: retrofit2.HttpException) {
                Log.e("BOOK_API", "HTTP Error: ${e.code()} ${e.message()}")

                if (e.code() == 429) {
                    Toast.makeText(
                        this@MainActivity,
                        "Google Books limit reached. Wait some time and avoid repeated searches.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Google Books error ${e.code()}. Try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: java.net.UnknownHostException) {
                Toast.makeText(
                    this@MainActivity,
                    "No internet connection.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: java.net.SocketTimeoutException) {
                Toast.makeText(
                    this@MainActivity,
                    "Search timed out. Try again.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.e("BOOK_API", "Unexpected Error: ${e.message}", e)

                Toast.makeText(
                    this@MainActivity,
                    "Search error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                isSearching = false
                searchButton.isEnabled = true
            }
        }
    }
    private fun hideDashboardButtons() {
        favoritesButton.visibility = View.GONE
        followUsersButton.visibility = View.GONE
        sharedBooksButton.visibility = View.GONE
        suggestionsButton.visibility = View.GONE
    }
    private fun showDashboardButtons() {
        favoritesButton.visibility = View.VISIBLE
        followUsersButton.visibility = View.VISIBLE
        sharedBooksButton.visibility = View.VISIBLE
        suggestionsButton.visibility = View.VISIBLE
    }
}