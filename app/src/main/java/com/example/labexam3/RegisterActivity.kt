package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.labexam3.databinding.ActivityRegisterBinding

/**
 * Register Activity - Handles user registration functionality
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        setupClickListeners()
        startAnimations()
    }

    /**
     * Sets up click listeners for form elements
     */
    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            performRegistration()
        }

        binding.textViewSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.imageViewBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    /**
     * Performs registration validation and account creation
     */
    private fun performRegistration() {
        val fullName = binding.editTextFullName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

        // Clear previous errors
        binding.textInputLayoutFullName.error = null
        binding.textInputLayoutEmail.error = null
        binding.textInputLayoutPassword.error = null
        binding.textInputLayoutConfirmPassword.error = null

        // Validation
        when {
            fullName.isEmpty() -> {
                binding.textInputLayoutFullName.error = "Full name is required"
                return
            }
            fullName.length < 2 -> {
                binding.textInputLayoutFullName.error = "Full name must be at least 2 characters"
                return
            }
            email.isEmpty() -> {
                binding.textInputLayoutEmail.error = "Email is required"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.textInputLayoutEmail.error = "Please enter a valid email"
                return
            }
            password.isEmpty() -> {
                binding.textInputLayoutPassword.error = "Password is required"
                return
            }
            password.length < 6 -> {
                binding.textInputLayoutPassword.error = "Password must be at least 6 characters"
                return
            }
            confirmPassword.isEmpty() -> {
                binding.textInputLayoutConfirmPassword.error = "Please confirm your password"
                return
            }
            password != confirmPassword -> {
                binding.textInputLayoutConfirmPassword.error = "Passwords do not match"
                return
            }
            !binding.checkBoxTerms.isChecked -> {
                Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Show loading state
        binding.buttonRegister.text = "Creating Account..."
        binding.buttonRegister.isEnabled = false

        // Simulate registration process
        binding.buttonRegister.postDelayed({
            registrationSuccessful(fullName, email, password)
        }, 2000)
    }

    /**
     * Handles successful registration
     */
    private fun registrationSuccessful(fullName: String, email: String, password: String) {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putBoolean("is_registered", true)
            putBoolean("is_logged_in", false)
            putString("user_name", fullName)
            putString("user_email", email)
            putString("user_password", password)
            apply()
        }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        Toast.makeText(this, "Welcome to Personal Wellnest! 🎉", Toast.LENGTH_LONG).show()
    }


    /**
     * Starts entrance animations
     */
    private fun startAnimations() {
        // Animate form container
        binding.scrollView.apply {
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .start()
        }

        // Animate header
        binding.textViewRegister.apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(200)
                .start()
        }

        // Animate subtitle
        binding.textViewCreateAccount.apply {
            alpha = 0f
            translationY = -30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(400)
                .start()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
