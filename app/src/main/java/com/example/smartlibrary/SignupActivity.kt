package com.example.smartlibrary

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val signupBtn = findViewById<Button>(R.id.signupButton)
        val loginRedirectText = findViewById<TextView>(R.id.loginRedirectText)

        loginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        signupBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(email, password)

            lifecycleScope.launch {
                BookDatabase.getDatabase(this@SignupActivity)
                    .userDao()
                    .insertUser(user)

                saveUserToFirebase(email)

                val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("currentUserEmail", email)
                    .putBoolean("isNewSignup", true)
                    .apply()

                Toast.makeText(this@SignupActivity, "Signup successful", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@SignupActivity, UserProfileActivity::class.java)
                intent.putExtra("fromSignup", true)
                startActivity(intent)
                finish()
            }
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