package com.example.labexam3

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.labexam3.databinding.ActivityHabitWidgetConfigureBinding

/**
 * Configuration Activity for the Habit Widget
 * Allows users to configure widget settings before adding it to home screen
 */
class HabitWidgetConfigureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHabitWidgetConfigureBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the result to CANCELED initially
        setResult(Activity.RESULT_CANCELED)

        // Get the app widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was not started with a valid widget ID, finish
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setupUI()
        showPreview()
    }

    /**
     * Sets up the user interface
     */
    private fun setupUI() {
        binding.textViewTitle.text = "Configure Habit Widget"
        binding.textViewDescription.text =
            "This widget will show your daily habit completion progress on your home screen."

        // Set up add button
        binding.buttonAddWidget.setOnClickListener {
            addWidget()
        }

        // Set up cancel button
        binding.buttonCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        // Set up preview update button
        binding.buttonUpdatePreview.setOnClickListener {
            showPreview()
        }
    }

    /**
     * Shows a preview of what the widget will look like
     */
    private fun showPreview() {
        val preferenceRepository = PreferenceRepository(this)
        val (completed, total) = preferenceRepository.getTodayCompletionStats()
        val percentage = if (total > 0) (completed * 100) / total else 0

        // Update preview text
        binding.textViewPreviewTitle.text = "Widget Preview"

        if (total > 0) {
            binding.textViewPreviewProgress.text = "Habits: $completed/$total"
            binding.textViewPreviewPercentage.text = "$percentage%"
        } else {
            binding.textViewPreviewProgress.text = "No habits yet"
            binding.textViewPreviewPercentage.text = "0%"
        }

        // Update preview progress bar
        binding.progressBarPreview.progress = percentage

        // Update preview emoji
        val emoji = when {
            percentage == 100 -> "🎉"
            percentage >= 80 -> "🌟"
            percentage >= 60 -> "💪"
            percentage >= 40 -> "🚀"
            percentage > 0 -> "🌱"
            else -> "📝"
        }
        binding.textViewPreviewEmoji.text = emoji

        // Update preview motivation
        val message = when {
            percentage == 100 -> "Complete!"
            percentage >= 80 -> "Almost there!"
            percentage >= 60 -> "Great progress!"
            percentage >= 40 -> "Keep going!"
            percentage > 0 -> "Good start!"
            else -> "Start today!"
        }
        binding.textViewPreviewMotivation.text = message

        // Show helpful information
        binding.textViewPreviewInfo.text = when {
            total == 0 -> "Add some habits in the app to see them here!"
            percentage == 0 -> "Complete your habits to see progress!"
            percentage < 100 -> "Complete more habits to reach 100%!"
            else -> "Congratulations on completing all your habits!"
        }
    }

    /**
     * Adds the widget to the home screen
     */
    private fun addWidget() {
        // Update the widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        HabitWidget().also { widget ->
            widget.onUpdate(this, appWidgetManager, intArrayOf(appWidgetId))
        }

        // Return success result with the widget ID
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    /**
     * Shows information about widget functionality
     */
    private fun showWidgetInfo() {
        val infoText = buildString {
            append("Widget Features:\n\n")
            append("• Shows today's habit completion progress\n")
            append("• Updates automatically when habits are completed\n")
            append("• Tap to open the app\n")
            append("• Displays motivational messages\n")
            append("• Shows completion percentage and count\n\n")
            append("The widget will refresh throughout the day as you complete your habits!")
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("About Habit Widget")
            .setMessage(infoText)
            .setPositiveButton("Got it", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh preview when activity becomes visible
        showPreview()
    }
}