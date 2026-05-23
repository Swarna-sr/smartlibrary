package com.example.smartlibrary

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val savedEmail = prefs.getString("currentUserEmail", "")

        if (isLoggedIn && !savedEmail.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val signupBtn = findViewById<Button>(R.id.signupButton)

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = BookDatabase.getDatabase(this@LoginActivity)
                    .userDao()
                    .login(email, password)

                if (user != null) {

                    prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("currentUserEmail", email)
                        .apply()

                    saveUserToFirebase(email)

                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this@LoginActivity, "Invalid login", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun saveUserToFirebase(email: String) {
        val userKey = FirebaseUtils.emailToKey(email)

        FirebaseDatabase.getInstance(FirebaseUtils.DATABASE_URL)
            .getReference("users")
            .child(userKey)
            .setValue(AppUser(email))
    }
}