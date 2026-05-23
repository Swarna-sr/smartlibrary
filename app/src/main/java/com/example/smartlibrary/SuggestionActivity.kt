package com.example.smartlibrary

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class SuggestionsActivity : AppCompatActivity() {

    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var suggestionText: TextView
    private lateinit var moodInput: EditText
    private lateinit var generateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestions)

        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView)
        suggestionText = findViewById(R.id.suggestionText)
        moodInput = findViewById(R.id.moodInput)
        generateButton = findViewById(R.id.generateButton)

        suggestionsRecyclerView.layoutManager = LinearLayoutManager(this)

        generateButton.setOnClickListener {
            loadInteractiveSuggestions()
        }
    }

    private fun loadInteractiveSuggestions() {
        lifecycleScope.launch {
            val favorites = BookDatabase
                .getDatabase(this@SuggestionsActivity)
                .favoriteBookDao()
                .getAllBooks()

            if (favorites.isEmpty()) {
                suggestionText.text = "Save some favorite books first ❤️"
                suggestionsRecyclerView.adapter = BookAdapter(emptyList())
                return@launch
            }

            val mood = moodInput.text.toString().trim()
            val latestFavorite = favorites.last()

            val query = if (mood.isNotEmpty()) {
                "$mood books similar to ${latestFavorite.title}"
            } else {
                "books similar to ${latestFavorite.title}"
            }

            suggestionText.text = "🤖 Finding: $query"

            try {
                val response = RetrofitInstance.api.searchBooks(query)
                suggestionsRecyclerView.adapter = BookAdapter(response.items)
            } catch (e: Exception) {
                suggestionText.text = "Could not load suggestions 😢"
            }
        }
    }
}