package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.labexam3.databinding.ActivitySplashscreenBinding

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashscreenBinding

    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        startAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DELAY)
    }

    private fun startAnimation() {
        binding.imageViewLogo.alpha = 0f
        binding.imageViewLogo.animate().alpha(1f).setDuration(1000).start()

        binding.textViewAppName.alpha = 0f
        binding.textViewAppName.animate().alpha(1f).setDuration(1000).setStartDelay(500).start()

        binding.textViewTagline.alpha = 0f
        binding.textViewTagline.animate().alpha(1f).setDuration(1000).setStartDelay(1000).start()
    }

//    private fun navigateToNextScreen() {
//        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
//        val isFirstLaunch = sharedPrefs.getBoolean("is_first_launch", true)
//        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
//
//        val intent = when {
//            isFirstLaunch -> {
//                // First install → onboarding
//                sharedPrefs.edit().putBoolean("is_first_launch", false).apply()
//                Intent(this, OnboardScreen1Activity::class.java)
//            }
//            isLoggedIn -> {
//                // Already logged in → main
//                Intent(this, MainActivity::class.java)
//            }
//            else -> {
//                // Returning user, not logged in → login/register
//                Intent(this, LoginRegisterActivity::class.java)
//            }
//        }
//
//        startActivity(intent)
//        finish()
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//    }



    private fun navigateToNextScreen() {
        // Always go to onboarding first
        val intent = Intent(this, OnboardScreen1Activity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
