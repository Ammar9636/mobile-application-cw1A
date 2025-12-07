package com.example.labexam3

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.labexam3.databinding.FragmentSettingsBinding

/**
 * Fragment for managing general app settings
 * Includes data management, theme options, and app information
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceRepository: PreferenceRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceRepository = PreferenceRepository(requireContext())

        setupDataManagement()
        setupNotificationPreferences()
        setupThemeSettings()
        setupStatsDisplay()
        setupAppInfo()
        setupLogout()
        loadCurrentSettings()
    }

    /**
     * Sets up data management options
     */
    private fun setupDataManagement() {
        // Clear all data button
        binding.buttonClearAllData.setOnClickListener {
            showClearDataConfirmationDialog()
        }

        // Clear habits only
        binding.buttonClearHabits.setOnClickListener {
            showClearHabitsConfirmationDialog()
        }

        // Clear mood entries only
        binding.buttonClearMoods.setOnClickListener {
            showClearMoodsConfirmationDialog()
        }

        // Export data button (placeholder for future implementation)
        binding.buttonExportData.setOnClickListener {
            exportDataToText()
        }

        // Reset settings
        binding.buttonResetSettings.setOnClickListener {
            showResetSettingsConfirmationDialog()
        }
    }

    /**
     * Sets up notification preferences
     */
    private fun setupNotificationPreferences() {
        // Enable/disable all notifications
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference("notifications_enabled", isChecked)
            updateNotificationSettings(isChecked)
        }

        // Vibration toggle
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference("vibration_enabled", isChecked)
        }

        // Sound toggle
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference("sound_enabled", isChecked)
        }
    }

    /**
     * Sets up theme and appearance settings
     */
    private fun setupThemeSettings() {
        // Theme selection
        binding.buttonSelectTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        // Font size selection
        binding.buttonFontSize.setOnClickListener {
            showFontSizeDialog()
        }
    }

    /**
     * Sets up statistics display
     */
    private fun setupStatsDisplay() {
        // Refresh stats button
        binding.buttonRefreshStats.setOnClickListener {
            updateStatsDisplay()
            showMessage("Statistics refreshed")
        }
    }

    /**
     * Sets up app information section
     */
    private fun setupAppInfo() {
        binding.textViewAppVersion.text = "Version 1.0.0"
        binding.textViewAppDescription.text =
            "Personal Wellness App helps you track your daily habits, " +
                    "log your moods, and stay hydrated with customizable reminders."

        // About button
        binding.buttonAbout.setOnClickListener {
            showAboutDialog()
        }

        // Privacy policy button
        binding.buttonPrivacyPolicy.setOnClickListener {
            showPrivacyPolicyDialog()
        }
    }

    /**
     * Sets up logout functionality
     */
    private fun setupLogout() {
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    /**
     * Loads current settings and updates UI
     */
    private fun loadCurrentSettings() {
        // Load notification preferences
        binding.switchNotifications.isChecked = getNotificationPreference("notifications_enabled", true)
        binding.switchVibration.isChecked = getNotificationPreference("vibration_enabled", true)
        binding.switchSound.isChecked = getNotificationPreference("sound_enabled", true)

        // Load theme settings
        val currentTheme = getThemePreference()
        binding.textViewCurrentTheme.text = getThemeDisplayName(currentTheme)

        val fontSize = getFontSizePreference()
        binding.textViewCurrentFontSize.text = getFontSizeDisplayName(fontSize)

        // Load statistics
        updateStatsDisplay()
    }

    /**
     * Updates notification settings state
     */
    private fun updateNotificationSettings(enabled: Boolean) {
        binding.switchVibration.isEnabled = enabled
        binding.switchSound.isEnabled = enabled

        if (!enabled) {
            showMessage("All notifications disabled")
        } else {
            showMessage("Notifications enabled")
        }
    }

    /**
     * Shows theme selection dialog
     */
    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Light", "Dark", "Auto (System Default)")
        val currentTheme = getThemePreference()
        val selectedIndex = when (currentTheme) {
            "light" -> 0
            "dark" -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val selectedTheme = when (which) {
                    0 -> "light"
                    1 -> "dark"
                    else -> "auto"
                }
                saveThemePreference(selectedTheme)
                binding.textViewCurrentTheme.text = themes[which]
                dialog.dismiss()
                showMessage("Theme updated. Restart app to apply changes.")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows font size selection dialog
     */
    private fun showFontSizeDialog() {
        val fontSizes = arrayOf("Small", "Medium", "Large")
        val currentSize = getFontSizePreference()
        val selectedIndex = when (currentSize) {
            "small" -> 0
            "medium" -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Font Size")
            .setSingleChoiceItems(fontSizes, selectedIndex) { dialog, which ->
                val selectedSize = when (which) {
                    0 -> "small"
                    1 -> "medium"
                    else -> "large"
                }
                saveFontSizePreference(selectedSize)
                binding.textViewCurrentFontSize.text = fontSizes[which]
                dialog.dismiss()
                showMessage("Font size updated")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows confirmation dialog for clearing all data
     */
    private fun showClearDataConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to delete all habits, mood entries, and settings? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows confirmation dialog for clearing habits only
     */
    private fun showClearHabitsConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Habits")
            .setMessage("Are you sure you want to delete all habits? This action cannot be undone.")
            .setPositiveButton("Clear Habits") { _, _ ->
                clearHabitsData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows confirmation dialog for clearing mood entries only
     */
    private fun showClearMoodsConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Mood Entries")
            .setMessage("Are you sure you want to delete all mood entries? This action cannot be undone.")
            .setPositiveButton("Clear Moods") { _, _ ->
                clearMoodData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows confirmation dialog for resetting settings
     */
    private fun showResetSettingsConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Settings")
            .setMessage("Are you sure you want to reset all settings to default values?")
            .setPositiveButton("Reset") { _, _ ->
                resetSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows logout confirmation dialog
     */
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? You'll need to sign in again to access your data.")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows about dialog
     */
    private fun showAboutDialog() {
        val aboutText = """
            Personal Wellness App v1.0.0
            
            Developed for IT2010 - Mobile Application Development
            BSc (Hons) Information Technology
            SLIIT - 2025
            
            Features:
            • Daily Habit Tracking
            • Mood Journal with Emoji Selector
            • Hydration Reminders
            • Home Screen Widget
            • Data Persistence with SharedPreferences
            
            Built with Android Studio & Kotlin
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("About Personal Wellness")
            .setMessage(aboutText)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Shows privacy policy dialog
     */
    private fun showPrivacyPolicyDialog() {
        val privacyText = """
            Privacy Policy
            
            Your privacy is important to us. This app:
            
            • Stores all data locally on your device
            • Does not collect personal information
            • Does not share data with third parties
            • Does not require internet connection
            • Uses only necessary permissions
            
            All your habits, mood entries, and settings remain private and secure on your device.
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Privacy Policy")
            .setMessage(privacyText)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Clears all app data
     */
    private fun clearAllData() {
        preferenceRepository.saveHabits(emptyList())
        preferenceRepository.saveMoodEntries(emptyList())
        resetSettings()
        updateStatsDisplay()
        showMessage("All data cleared successfully")
    }

    /**
     * Clears only habits data
     */
    private fun clearHabitsData() {
        preferenceRepository.saveHabits(emptyList())
        updateStatsDisplay()
        showMessage("All habits cleared successfully")
    }

    /**
     * Clears only mood data
     */
    private fun clearMoodData() {
        preferenceRepository.saveMoodEntries(emptyList())
        updateStatsDisplay()
        showMessage("All mood entries cleared successfully")
    }

    /**
     * Exports data as text (placeholder for future enhancement)
     */
    private fun exportDataToText() {
        val habits = preferenceRepository.loadHabits()
        val moods = preferenceRepository.loadMoodEntries()

        val exportText = buildString {
            append("Personal Wellness Data Export\n")
            append("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")

            append("HABITS (${habits.size}):\n")
            habits.forEach { habit ->
                append("- ${habit.title}\n")
                append("  Description: ${habit.description}\n")
                append("  Completed dates: ${habit.completedDates.size}\n")
                append("  Current streak: ${habit.getStreak()}\n\n")
            }

            append("MOOD ENTRIES (${moods.size}):\n")
            moods.forEach { mood ->
                append("- ${mood.getFormattedDate()}: ${mood.emoji} ${mood.moodName}\n")
                if (mood.note.isNotEmpty()) {
                    append("  Note: ${mood.note}\n")
                }
                append("\n")
            }
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, exportText)
            putExtra(Intent.EXTRA_SUBJECT, "Personal Wellness Data Export")
        }

        startActivity(Intent.createChooser(shareIntent, "Export Data"))
    }

    /**
     * Resets all settings to default values
     */
    private fun resetSettings() {
        val sharedPrefs = requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        loadCurrentSettings()
        showMessage("Settings reset to defaults")
    }

    /**
     * Performs logout
     */
    private fun performLogout() {
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putBoolean("is_logged_in", false)
            apply()
        }

        val intent = Intent(requireContext(), LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }


    /**
     * Updates the statistics display
     */
    private fun updateStatsDisplay() {
        val habits = preferenceRepository.loadHabits().filter { it.isActive }
        val moodEntries = preferenceRepository.loadMoodEntries()
        val (completed, total) = preferenceRepository.getTodayCompletionStats()

        binding.textViewHabitCount.text = "Active Habits: ${habits.size}"
        binding.textViewTodayProgress.text = "Today's Progress: $completed/$total completed"
        binding.textViewMoodEntries.text = "Total Mood Entries: ${moodEntries.size}"

        // Calculate streak information
        val streaks = habits.map { it.getStreak() }.filter { it > 0 }
        if (streaks.isNotEmpty()) {
            val maxStreak = streaks.maxOrNull() ?: 0
            val avgStreak = streaks.average()
            binding.textViewStreakInfo.text =
                "Best Streak: $maxStreak days | Avg: ${String.format("%.1f", avgStreak)} days"
        } else {
            binding.textViewStreakInfo.text = "No active streaks yet"
        }

        // Data size information
        val totalDataPoints = habits.size + moodEntries.size
        binding.textViewDataSize.text = "Total Data Points: $totalDataPoints"
    }

    /**
     * Helper methods for preferences
     */
    private fun saveNotificationPreference(key: String, value: Boolean) {
        requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean(key, value).apply()
    }

    private fun getNotificationPreference(key: String, defaultValue: Boolean): Boolean {
        return requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .getBoolean(key, defaultValue)
    }

    private fun saveThemePreference(theme: String) {
        requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .edit().putString("theme", theme).apply()
    }

    private fun getThemePreference(): String {
        return requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .getString("theme", "auto") ?: "auto"
    }

    private fun getThemeDisplayName(theme: String): String {
        return when (theme) {
            "light" -> "Light"
            "dark" -> "Dark"
            else -> "Auto (System Default)"
        }
    }

    private fun saveFontSizePreference(size: String) {
        requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .edit().putString("font_size", size).apply()
    }

    private fun getFontSizePreference(): String {
        return requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .getString("font_size", "medium") ?: "medium"
    }

    private fun getFontSizeDisplayName(size: String): String {
        return when (size) {
            "small" -> "Small"
            "large" -> "Large"
            else -> "Medium"
        }
    }

    /**
     * Shows a message to the user
     */
    private fun showMessage(message: String) {
        val successView = binding.textViewMessage
        successView.text = message
        successView.visibility = View.VISIBLE

        // Hide after 3 seconds
        successView.postDelayed({
            if (_binding != null) {
                successView.visibility = View.GONE
            }
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        loadCurrentSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}