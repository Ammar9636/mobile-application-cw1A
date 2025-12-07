package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.labexam3.databinding.ActivityOnboardscreen2Binding

/**
 * Second Onboarding Screen - Introduces mood tracking feature
 */
class OnboardScreen2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardscreen2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardscreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        setupClickListeners()
        startAnimations()
    }

    /**
     * Sets up click listeners for navigation buttons
     */
    private fun setupClickListeners() {
        binding.buttonNext.setOnClickListener {
            val intent = Intent(this, OnboardScreen3Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.buttonPrevious.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.textViewSkip.setOnClickListener {
            skipToLoginRegister()
        }
    }

    /**
     * Starts entrance animations
     */
    private fun startAnimations() {
        // Animate image
        binding.imageViewOnboard.apply {
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .start()
        }

        // Animate title
        binding.textViewTitle.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(200)
                .start()
        }

        // Animate description
        binding.textViewDescription.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(400)
                .start()
        }

        // Animate navigation buttons
        binding.linearLayoutNavigation.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(600)
                .start()
        }
    }

    /**
     * Skips remaining onboarding and goes to login/register
     */
    private fun skipToLoginRegister() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}