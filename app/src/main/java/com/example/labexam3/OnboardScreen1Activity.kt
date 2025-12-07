package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.labexam3.databinding.ActivityOnboardscreen1Binding

/**
 * First Onboarding Screen - Introduces habit tracking feature
 */
class OnboardScreen1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardscreen1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardscreen1Binding.inflate(layoutInflater)
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
            val intent = Intent(this, OnboardScreen2Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.textViewSkip.setOnClickListener {
            skipToLoginRegister()
        }

        binding.buttonGetStarted.setOnClickListener {
            val intent = Intent(this, OnboardScreen2Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

        // Animate buttons
        binding.buttonNext.apply {
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
     * Skips onboarding and goes directly to login/register
     */
    private fun skipToLoginRegister() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}