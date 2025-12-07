package com.example.labexam3

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.labexam3.databinding.ActivityMainBinding

/**
 * Main Activity that hosts the bottom navigation and manages fragment transitions
 * Uses BottomNavigationView to switch between three main fragments:
 * HabitTrackerFragment, MoodJournalFragment, and SettingsFragment
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        // Initialize data binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up bottom navigation
        setupBottomNavigation()

        // Load default fragment if no saved state
        if (savedInstanceState == null) {
            loadFragment(HabitTrackerFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> {
                    loadFragment(HabitTrackerFragment())
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodJournalFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                R.id.nav_hyd ->{
                    loadFragment(HydrationReminderFragment())
                    true
                }
                R.id.nav_profile -> {  // Changed from nav_settings
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
