package com.example.smartlibrary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var currentEmailText: TextView
    private lateinit var saveProfileButton: Button
    private lateinit var backToHomeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        nameInput = findViewById(R.id.nameInput)
        currentEmailText = findViewById(R.id.currentEmailText)
        saveProfileButton = findViewById(R.id.saveProfileButton)
        backToHomeButton = findViewById(R.id.backToHomeButton)

        val loginPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val profilePrefs = getSharedPreferences("UserProfile", MODE_PRIVATE)

        val currentEmail = loginPrefs.getString("currentUserEmail", "") ?: ""
        val savedName = profilePrefs.getString("userName", "") ?: ""

        currentEmailText.text = if (currentEmail.isNotEmpty()) {
            currentEmail
        } else {
            "Not logged in"
        }

        nameInput.setText(savedName)

        saveProfileButton.setOnClickListener {
            val name = nameInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            profilePrefs.edit()
                .putString("userName", name)
                .apply()

            loginPrefs.edit()
                .putBoolean("isNewSignup", false)
                .apply()

            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        backToHomeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}