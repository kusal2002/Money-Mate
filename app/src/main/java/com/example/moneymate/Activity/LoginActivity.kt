package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var prefsHelper: PrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefsHelper = PrefsHelper(this)

        // Check if user is already logged in
        if (prefsHelper.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Schedule daily expense reminder if enabled
        if (prefsHelper.isDailyExpenseReminderEnabled()) {
            ExpenseReminderReceiver.scheduleDailyReminder(this)
        }

        // Show welcome message if a user has registered
        val storedUsername = prefsHelper.getUsername()
//        if (!storedUsername.isNullOrEmpty() && storedUsername != "John Smith") { // Exclude default value
//            Toast.makeText(this, "Welcome back, $storedUsername", Toast.LENGTH_LONG).show()
//        }

        val usernameEditText: TextInputEditText = findViewById(R.id.usernameEditText)
        val passwordEditText: TextInputEditText = findViewById(R.id.passwordEditText)
        val loginButton: TextView = findViewById(R.id.loginButton)
        val registerLink: TextView = findViewById(R.id.registerLink)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if a user is registered
            val storedUsernameForLogin = prefsHelper.getUsername()
            val storedPassword = prefsHelper.getPassword()

            if (storedUsernameForLogin.isNullOrEmpty() || storedPassword.isNullOrEmpty()) {
                Toast.makeText(this, "No account found. Please register first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate credentials
            if (username == storedUsernameForLogin && password == storedPassword) {
                prefsHelper.setLoggedIn(true)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}