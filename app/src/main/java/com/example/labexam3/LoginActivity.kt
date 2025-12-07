package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.labexam3.databinding.ActivityLogin2Binding

/**
 * Login Activity - Handles user login functionality
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogin2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        setupClickListeners()
        startAnimations()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            performLogin()
        }

        binding.textViewSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.textViewForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.imageViewBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun performLogin() {
        val emailInput = binding.editTextEmail.text.toString().trim()
        val passwordInput = binding.editTextPassword.text.toString().trim()

        // Basic validation
        when {
            emailInput.isEmpty() -> {
                binding.textInputLayoutEmail.error = "Email is required"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches() -> {
                binding.textInputLayoutEmail.error = "Please enter a valid email"
                return
            }
            passwordInput.isEmpty() -> {
                binding.textInputLayoutPassword.error = "Password is required"
                return
            }
            passwordInput.length < 6 -> {
                binding.textInputLayoutPassword.error = "Password must be at least 6 characters"
                return
            }
        }

        // Clear errors
        binding.textInputLayoutEmail.error = null
        binding.textInputLayoutPassword.error = null

        // Loading state
        binding.buttonLogin.text = "Logging in..."
        binding.buttonLogin.isEnabled = false

        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("user_email", null)
        val savedPassword = sharedPrefs.getString("user_password", null)

        binding.buttonLogin.postDelayed({
            if (emailInput == savedEmail && passwordInput == savedPassword) {
                loginSuccessful(emailInput, passwordInput)
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                binding.buttonLogin.text = "Login"
                binding.buttonLogin.isEnabled = true
            }
        }, 1000)
    }

    private fun loginSuccessful(email: String, password: String) {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("user_email", email)
            putString("user_password", password)
            apply()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        Toast.makeText(this, "Welcome back! 👋", Toast.LENGTH_SHORT).show()
    }

    private fun startAnimations() {
        binding.cardViewLoginForm.apply {
            alpha = 0f
            translationY = 100f
            animate().alpha(1f).translationY(0f).setDuration(800).start()
        }

        binding.textViewLogin.apply {
            alpha = 0f
            translationY = -50f
            animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(200).start()
        }

        binding.textViewWelcomeBack.apply {
            alpha = 0f
            translationY = -30f
            animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(400).start()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
