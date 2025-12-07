package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.labexam3.databinding.ActivityOnboardscreen3Binding

/**
 * Third Onboarding Screen - Introduces hydration reminders and completes onboarding
 */
class OnboardScreen3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardscreen3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardscreen3Binding.inflate(layoutInflater)
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
        binding.buttonGetStarted.setOnClickListener {
            completeOnboarding()
        }

        binding.buttonPrevious.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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

        // Animate features list
        binding.linearLayoutFeatures.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(600)
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
                .setStartDelay(800)
                .start()
        }
    }

    /**
     * Completes onboarding and navigates to login/register screen
     */
//    private fun completeOnboarding() {
//        // Mark onboarding as completed
//        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
//        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
//
//        // Navigate to login/register
//        val intent = Intent(this, LoginRegisterActivity::class.java)
//        startActivity(intent)
//        finish()
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//    }

    private fun completeOnboarding() {
        // Navigate to login/register screen
        val intent = Intent(this, LoginRegisterActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}