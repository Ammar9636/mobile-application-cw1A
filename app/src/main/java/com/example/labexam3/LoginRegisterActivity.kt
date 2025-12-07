package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.labexam3.databinding.ActivityLoginregisterBinding


class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginregisterBinding

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        //  Check if already logged in
//        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
//        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
//
//        if (isLoggedIn) {
//            // User already logged in → go directly to MainActivity
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//            return
//        }
//
//        // Otherwise → show login/register screen
//        binding = ActivityLoginregisterBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Hide action bar
//        supportActionBar?.hide()
//
//        setupClickListeners()
//        startAnimations()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginregisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        setupClickListeners()
        startAnimations()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.textViewGuestLogin?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    /**
     * Starts entrance animations
     */
    private fun startAnimations() {
        // Animate logo
        binding.imageViewLogo.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .start()
        }

        // Animate app name
        binding.textViewAppName.apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(200)
                .start()
        }

        // Animate tagline
        binding.textViewTagline.apply {
            alpha = 0f
            translationY = -30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(400)
                .start()
        }

        // Animate welcome message
        binding.textViewWelcome.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(600)
                .start()
        }

        // Animate login button
        binding.buttonLogin.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(800)
                .start()
        }

        // Animate register button
        binding.buttonRegister.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(1000)
                .start()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}