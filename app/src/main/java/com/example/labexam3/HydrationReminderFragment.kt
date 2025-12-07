package com.example.labexam3

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.labexam3.databinding.FragmentHydrationReminderBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for managing hydration reminder settings
 * Includes interval selection, notification customization, and scheduling options
 */
class HydrationReminderFragment : Fragment() {

    private var _binding: FragmentHydrationReminderBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceRepository: PreferenceRepository
    private lateinit var reminderScheduler: ReminderScheduler

    // Hydration interval presets in minutes
    private val hydrationPresets = listOf(

        15 to "Every 15 minutes",
        30 to "Every 30 minutes",
        60 to "Every 1 hour",
        120 to "Every 2 hours",
        180 to "Every 3 hours",
        240 to "Every 4 hours",
        360 to "Every 6 hours"
    )

    // Snooze duration options in minutes
    private val snoozeDurations = listOf(
        15 to "15 minutes",
        30 to "30 minutes",
        60 to "1 hour",
        120 to "2 hours",
        480 to "8 hours (until morning)"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceRepository = PreferenceRepository(requireContext())
        reminderScheduler = ReminderScheduler(requireContext())

        setupReminderToggle()
        setupIntervalSettings()
        setupSnoozeSettings()
        setupNotificationCustomization()
        setupTestingOptions()
        setupScheduleDisplay()
        loadCurrentSettings()
    }

    /**
     * Sets up the main reminder toggle
     */
    private fun setupReminderToggle() {
        binding.switchHydrationReminder.setOnCheckedChangeListener { _, isChecked ->
            preferenceRepository.setHydrationEnabled(isChecked)
            updateReminderState(isChecked)
            updateScheduleDisplay()
        }
    }

    /**
     * Sets up interval selection options
     */
    private fun setupIntervalSettings() {
        // Preset interval buttons
        binding.buttonPreset15min.setOnClickListener { selectPresetInterval(15) }
        binding.buttonPreset1hour.setOnClickListener { selectPresetInterval(60) }
        binding.buttonPreset2hours.setOnClickListener { selectPresetInterval(120) }
        binding.buttonPreset3hours.setOnClickListener { selectPresetInterval(180) }

        // Custom interval button
        binding.buttonCustomInterval.setOnClickListener {
            showCustomIntervalDialog()
        }

        // Time picker for precise timing
        binding.buttonTimePicker.setOnClickListener {
            showTimePickerDialog()
        }

        // Interval selection from list
        binding.buttonSelectFromList.setOnClickListener {
            showIntervalSelectionDialog()
        }
    }

    /**
     * Sets up snooze functionality
     */
    private fun setupSnoozeSettings() {
        // Enable snooze toggle
        binding.switchEnableSnooze.setOnCheckedChangeListener { _, isChecked ->
            saveSnoozePreferenceBoolean("snooze_enabled", isChecked)
            binding.layoutSnoozeOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Snooze duration selection
        binding.buttonSnoozeDuration.setOnClickListener {
            showSnoozeDurationDialog()
        }

        // Snooze now button
        binding.buttonSnoozeNow.setOnClickListener {
            snoozeRemindersNow()
        }
    }

    /**
     * Sets up notification customization options
     */
    private fun setupNotificationCustomization() {
        // Sound selection
        binding.buttonSelectSound.setOnClickListener {
            selectNotificationSound()
        }

        // Vibration pattern toggle - FIXED
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreferenceBoolean("vibration_enabled", isChecked)
        }

        // Priority level selection
        binding.buttonPriority.setOnClickListener {
            showPrioritySelectionDialog()
        }

        // LED color selection (for supported devices)
        binding.buttonLedColor.setOnClickListener {
            showLedColorSelectionDialog()
        }
    }

    /**
     * Sets up testing and preview options
     */
    private fun setupTestingOptions() {
        // Test notification button
        binding.buttonTestNotification.setOnClickListener {
            reminderScheduler.sendTestReminder()
            showMessage("Test notification sent! Check your notification panel.")
        }

        // Preview notification button
        binding.buttonPreviewNotification.setOnClickListener {
            showNotificationPreview()
        }

        // Test sound button
        binding.buttonTestSound.setOnClickListener {
            testNotificationSound()
        }

        // Test vibration button
        binding.buttonTestVibration.setOnClickListener {
            testVibrationPattern()
        }
    }

    /**
     * Sets up schedule display and management
     */
    private fun setupScheduleDisplay() {
        // Refresh schedule button
        binding.buttonRefreshSchedule.setOnClickListener {
            updateScheduleDisplay()
            showMessage("Schedule refreshed")
        }

        // Clear schedule button
        binding.buttonClearSchedule.setOnClickListener {
            clearReminderSchedule()
        }
    }

    /**
     * Loads current settings and updates UI
     */
    private fun loadCurrentSettings() {
        // Load main toggle
        val isEnabled = preferenceRepository.isHydrationEnabled()
        binding.switchHydrationReminder.isChecked = isEnabled
        updateReminderState(isEnabled)

        // Load interval
        val interval = preferenceRepository.getHydrationInterval()
        updateIntervalDisplay(interval)
        updatePresetButtons(interval)

        // Load snooze settings
        val snoozeEnabled = getSnoozePreferenceBoolean("snooze_enabled", true)
        binding.switchEnableSnooze.isChecked = snoozeEnabled
        binding.layoutSnoozeOptions.visibility = if (snoozeEnabled) View.VISIBLE else View.GONE

        val snoozeDuration = getSnoozePreferenceInt("snooze_duration", 30)
        updateSnoozeDurationDisplay(snoozeDuration)

        // Load notification settings - FIXED
        binding.switchVibration.isChecked = getNotificationPreferenceBoolean("vibration_enabled", true)
        updateSoundDisplay()
        updatePriorityDisplay()
        updateLedColorDisplay()

        // Update schedule display
        updateScheduleDisplay()
    }

    /**
     * Updates the reminder state and related UI elements
     */
    private fun updateReminderState(isEnabled: Boolean) {
        binding.layoutIntervalSettings.visibility = if (isEnabled) View.VISIBLE else View.GONE
        binding.layoutNotificationCustomization.visibility = if (isEnabled) View.VISIBLE else View.GONE
        binding.layoutTestingOptions.visibility = if (isEnabled) View.VISIBLE else View.GONE

        if (isEnabled) {
            reminderScheduler.scheduleReminder()
            binding.textViewReminderStatus.text = "Hydration reminders are active"
            binding.textViewReminderStatus.setTextColor(requireContext().getColor(R.color.accent_success))
        } else {
            reminderScheduler.cancelReminder()
            binding.textViewReminderStatus.text = "Hydration reminders are disabled"
            binding.textViewReminderStatus.setTextColor(requireContext().getColor(R.color.text_secondary))
        }
    }

    /**
     * Selects a preset interval
     */
    private fun selectPresetInterval(intervalMinutes: Int) {
        preferenceRepository.setHydrationInterval(intervalMinutes)
        updateIntervalDisplay(intervalMinutes)
        updatePresetButtons(intervalMinutes)

        if (preferenceRepository.isHydrationEnabled()) {
            reminderScheduler.scheduleReminder()
            updateScheduleDisplay()
        }

        showMessage("Reminder interval set to ${getIntervalDisplayText(intervalMinutes)}")
    }

    /**
     * Shows custom interval input dialog
     */
    private fun showCustomIntervalDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_interval, null)
        val editTextHours = dialogView.findViewById<android.widget.EditText>(R.id.editTextHours)
        val editTextMinutes = dialogView.findViewById<android.widget.EditText>(R.id.editTextMinutes)

        AlertDialog.Builder(requireContext())
            .setTitle("Set Custom Interval")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val hours = editTextHours.text.toString().toIntOrNull() ?: 0
                val minutes = editTextMinutes.text.toString().toIntOrNull() ?: 0
                val totalMinutes = (hours * 60) + minutes

                if (totalMinutes >= 15) {
                    selectPresetInterval(totalMinutes)
                } else {
                    showMessage("Minimum interval is 15 minutes")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows time picker for precise interval setting
     */
    private fun showTimePickerDialog() {
        val currentInterval = preferenceRepository.getHydrationInterval()
        val hours = currentInterval / 60
        val minutes = currentInterval % 60

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val totalMinutes = (selectedHour * 60) + selectedMinute
            if (totalMinutes >= 15) {
                selectPresetInterval(totalMinutes)
            } else {
                showMessage("Minimum interval is 15 minutes")
            }
        }, hours, minutes, true).show()
    }

    /**
     * Shows interval selection dialog with all presets
     */
    private fun showIntervalSelectionDialog() {
        val options = hydrationPresets.map { it.second }.toTypedArray()
        val currentInterval = preferenceRepository.getHydrationInterval()
        val selectedIndex = hydrationPresets.indexOfFirst { it.first == currentInterval }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Reminder Interval")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val selectedInterval = hydrationPresets[which].first
                selectPresetInterval(selectedInterval)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows snooze duration selection dialog
     */
    private fun showSnoozeDurationDialog() {
        val options = snoozeDurations.map { it.second }.toTypedArray()
        val currentDuration = getSnoozePreferenceInt("snooze_duration", 30)
        val selectedIndex = snoozeDurations.indexOfFirst { it.first == currentDuration }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Snooze Duration")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val selectedDuration = snoozeDurations[which].first
                saveSnoozePreferenceInt("snooze_duration", selectedDuration)
                updateSnoozeDurationDisplay(selectedDuration)
                dialog.dismiss()
                showMessage("Snooze duration updated")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Snoozes reminders for the selected duration
     */
    private fun snoozeRemindersNow() {
        val duration = getSnoozePreferenceInt("snooze_duration", 30)
        reminderScheduler.cancelReminder()

        // Schedule reminder after snooze duration
        val delayMillis = duration * 60 * 1000L
        reminderScheduler.scheduleSnoozeReminder()

        val durationText = snoozeDurations.find { it.first == duration }?.second ?: "$duration minutes"
        showMessage("Reminders snoozed for $durationText")

        updateScheduleDisplay()
    }

    /**
     * Selects notification sound
     */
    private fun selectNotificationSound() {
        val intent = android.content.Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Hydration Reminder Sound")
        }
        startActivityForResult(intent, REQUEST_CODE_SOUND)
    }

    /**
     * Shows notification priority selection dialog
     */
    private fun showPrioritySelectionDialog() {
        val priorities = arrayOf("Low", "Default", "High", "Max")
        val currentPriority = getNotificationPreferenceInt("priority", 1)

        AlertDialog.Builder(requireContext())
            .setTitle("Notification Priority")
            .setSingleChoiceItems(priorities, currentPriority) { dialog, which ->
                saveNotificationPreferenceInt("priority", which)
                updatePriorityDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows LED color selection dialog
     */
    private fun showLedColorSelectionDialog() {
        val colors = arrayOf("Default", "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta", "White")
        val colorValues = arrayOf(0, 0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt(),
            0xFFFFFF00.toInt(), 0xFF00FFFF.toInt(), 0xFFFF00FF.toInt(), 0xFFFFFFFF.toInt())
        val currentColor = getNotificationPreferenceInt("led_color", 0)
        val selectedIndex = colorValues.indexOf(currentColor)

        AlertDialog.Builder(requireContext())
            .setTitle("LED Color")
            .setSingleChoiceItems(colors, selectedIndex) { dialog, which ->
                saveNotificationPreferenceInt("led_color", colorValues[which])
                updateLedColorDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows notification preview
     */
    private fun showNotificationPreview() {
        val previewText = buildString {
            append("Notification Preview:\n\n")
            append("Title: Hydration Reminder\n")
            append("Message: Time to drink water! Stay hydrated!\n")
            append("Sound: ${getSoundDisplayName()}\n")
            append("Vibration: ${if (getNotificationPreferenceBoolean("vibration_enabled", true)) "Enabled" else "Disabled"}\n")
            append("Priority: ${getPriorityDisplayName()}\n")
            append("LED Color: ${getLedColorDisplayName()}")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Notification Preview")
            .setMessage(previewText)
            .setPositiveButton("Send Test") { _, _ ->
                reminderScheduler.sendTestReminder()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    /**
     * Tests notification sound
     */
    private fun testNotificationSound() {
        try {
            val soundUri = getSavedSoundUri()
            val ringtone = RingtoneManager.getRingtone(requireContext(), soundUri)
            ringtone?.play()
            showMessage("Playing notification sound...")
        } catch (e: Exception) {
            showMessage("Could not play sound")
        }
    }

    /**
     * Tests vibration pattern
     */
    private fun testVibrationPattern() {
        if (getNotificationPreferenceBoolean("vibration_enabled", true)) {
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 500, 250, 500), -1))
            } else {
                vibrator.vibrate(longArrayOf(0, 500, 250, 500), -1)
            }
            showMessage("Testing vibration pattern...")
        } else {
            showMessage("Vibration is disabled")
        }
    }

    /**
     * Clears reminder schedule
     */
    private fun clearReminderSchedule() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Schedule")
            .setMessage("This will cancel all pending hydration reminders. Are you sure?")
            .setPositiveButton("Clear") { _, _ ->
                reminderScheduler.cancelReminder()
                updateScheduleDisplay()
                showMessage("Reminder schedule cleared")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Updates interval display
     */
    private fun updateIntervalDisplay(intervalMinutes: Int) {
        binding.textViewCurrentInterval.text = getIntervalDisplayText(intervalMinutes)
    }

    /**
     * Updates preset button states
     */
    private fun updatePresetButtons(currentInterval: Int) {
        binding.buttonPreset15min.isSelected = currentInterval == 15
        binding.buttonPreset1hour.isSelected = currentInterval == 60
        binding.buttonPreset2hours.isSelected = currentInterval == 120
        binding.buttonPreset3hours.isSelected = currentInterval == 180
    }

    /**
     * Updates snooze duration display
     */
    private fun updateSnoozeDurationDisplay(durationMinutes: Int) {
        val displayText = snoozeDurations.find { it.first == durationMinutes }?.second ?: "$durationMinutes minutes"
        binding.textViewSnoozeDuration.text = displayText
    }

    /**
     * Updates sound display
     */
    private fun updateSoundDisplay() {
        binding.textViewCurrentSound.text = getSoundDisplayName()
    }

    /**
     * Updates priority display
     */
    private fun updatePriorityDisplay() {
        binding.textViewCurrentPriority.text = getPriorityDisplayName()
    }

    /**
     * Updates LED color display
     */
    private fun updateLedColorDisplay() {
        binding.textViewCurrentLedColor.text = getLedColorDisplayName()
    }

    /**
     * Updates schedule display with next reminder time
     */
    private fun updateScheduleDisplay() {
        if (preferenceRepository.isHydrationEnabled()) {
            val nextReminderTime = reminderScheduler.getNextReminderTimeString()
            val interval = preferenceRepository.getHydrationInterval()

            binding.textViewNextReminder.text = nextReminderTime
            binding.textViewScheduleStatus.text = "Active - ${getIntervalDisplayText(interval)}"
            binding.textViewScheduleStatus.setTextColor(requireContext().getColor(R.color.accent_success))

            val activeReminders = if (reminderScheduler.areRemindersScheduled()) "Yes" else "No"
            binding.textViewActiveReminders.text = "Active reminders: $activeReminders"
        } else {
            binding.textViewNextReminder.text = "Not scheduled"
            binding.textViewScheduleStatus.text = "Disabled"
            binding.textViewScheduleStatus.setTextColor(requireContext().getColor(R.color.text_secondary))
            binding.textViewActiveReminders.text = "Active reminders: No"
        }

        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        binding.textViewLastUpdated.text = "Last updated: $currentTime"
    }

    /**
     * Helper methods with distinct names to avoid type conflicts
     */
    private fun getIntervalDisplayText(intervalMinutes: Int): String {
        return when {
            intervalMinutes < 60 -> "$intervalMinutes minutes"
            intervalMinutes == 60 -> "1 hour"
            intervalMinutes % 60 == 0 -> "${intervalMinutes / 60} hours"
            else -> {
                val hours = intervalMinutes / 60
                val minutes = intervalMinutes % 60
                "$hours hours and $minutes minutes"
            }
        }
    }

    private fun saveNotificationPreferenceInt(key: String, value: Int) {
        requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .edit().putInt(key, value).apply()
    }

    private fun saveNotificationPreferenceBoolean(key: String, value: Boolean) {
        requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .edit().putBoolean(key, value).apply()
    }

    private fun getNotificationPreferenceInt(key: String, defaultValue: Int): Int {
        return requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .getInt(key, defaultValue)
    }

    private fun getNotificationPreferenceBoolean(key: String, defaultValue: Boolean): Boolean {
        return requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .getBoolean(key, defaultValue)
    }

    private fun saveSnoozePreferenceInt(key: String, value: Int) {
        requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .edit().putInt(key, value).apply()
    }

    private fun saveSnoozePreferenceBoolean(key: String, value: Boolean) {
        requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .edit().putBoolean(key, value).apply()
    }

    private fun getSnoozePreferenceInt(key: String, defaultValue: Int): Int {
        return requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .getInt(key, defaultValue)
    }

    private fun getSnoozePreferenceBoolean(key: String, defaultValue: Boolean): Boolean {
        return requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .getBoolean(key, defaultValue)
    }

    private fun getSavedSoundUri(): android.net.Uri {
        val soundUriString = requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
            .getString("sound_uri", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString())
        return android.net.Uri.parse(soundUriString)
    }

    private fun getSoundDisplayName(): String {
        val soundUri = getSavedSoundUri()
        return try {
            val ringtone = RingtoneManager.getRingtone(requireContext(), soundUri)
            ringtone?.getTitle(requireContext()) ?: "Default"
        } catch (e: Exception) {
            "Default"
        }
    }

    private fun getPriorityDisplayName(): String {
        val priority = getNotificationPreferenceInt("priority", 1)
        return when (priority) {
            0 -> "Low"
            1 -> "Default"
            2 -> "High"
            3 -> "Max"
            else -> "Default"
        }
    }

    private fun getLedColorDisplayName(): String {
        val color = getNotificationPreferenceInt("led_color", 0)
        return when (color) {
            0xFFFF0000.toInt() -> "Red"
            0xFF00FF00.toInt() -> "Green"
            0xFF0000FF.toInt() -> "Blue"
            0xFFFFFF00.toInt() -> "Yellow"
            0xFF00FFFF.toInt() -> "Cyan"
            0xFFFF00FF.toInt() -> "Magenta"
            0xFFFFFFFF.toInt() -> "White"
            else -> "Default"
        }
    }

    /**
     * Shows a message to the user
     */
    private fun showMessage(message: String) {
        val messageView = binding.textViewMessage
        messageView.text = message
        messageView.visibility = View.VISIBLE

        messageView.postDelayed({
            if (_binding != null) {
                messageView.visibility = View.GONE
            }
        }, 3000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SOUND && resultCode == android.app.Activity.RESULT_OK) {
            val soundUri = data?.getParcelableExtra<android.net.Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (soundUri != null) {
                requireContext().getSharedPreferences("hydration_settings", Context.MODE_PRIVATE)
                    .edit().putString("sound_uri", soundUri.toString()).apply()
                updateSoundDisplay()
                showMessage("Notification sound updated")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCurrentSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_SOUND = 1001
    }
}