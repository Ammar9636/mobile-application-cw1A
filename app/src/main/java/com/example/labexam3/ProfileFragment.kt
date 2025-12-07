package com.example.labexam3

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.labexam3.databinding.FragmentProfileBinding
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * Profile Fragment - User health profile and daily health tracking
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var healthProfile: HealthProfile
    private val dailyLogs = mutableListOf<DailyHealthLog>()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadHealthProfile()
        setupProfileSection()
        setupDailyMetricsSection()
        setupVitalsSection()
        setupMedicalInfoSection()
        displayProfileData()
        updateHealthInsights()
    }

    /**
     * Loads health profile from SharedPreferences
     */
    private fun loadHealthProfile() {
        val prefs = requireContext().getSharedPreferences("health_profile", android.content.Context.MODE_PRIVATE)
        val json = prefs.getString("profile_data", null)

        healthProfile = if (json != null) {
            gson.fromJson(json, HealthProfile::class.java)
        } else {
            // Load user name from login if available
            val appPrefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            val userName = appPrefs.getString("user_name", "")
            HealthProfile(fullName = userName ?: "")
        }

        loadDailyLogs()
    }
    /**
     * Saves health profile to SharedPreferences
     */
    private fun saveHealthProfile() {
        val prefs = requireContext().getSharedPreferences("health_profile", android.content.Context.MODE_PRIVATE)
        val json = gson.toJson(healthProfile)
        prefs.edit().putString("profile_data", json).apply()

        showMessage("Profile saved successfully!")
        displayProfileData()
        updateHealthInsights()
    }

    /**
     * Loads daily health logs
     */
    private fun loadDailyLogs() {
        val prefs = requireContext().getSharedPreferences("health_profile", android.content.Context.MODE_PRIVATE)
        val json = prefs.getString("daily_logs", null)

        if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<DailyHealthLog>>() {}.type
            val logs = gson.fromJson<List<DailyHealthLog>>(json, type)
            dailyLogs.clear()
            dailyLogs.addAll(logs ?: emptyList())
        }
    }

    /**
     * Saves daily health logs
     */
    private fun saveDailyLogs() {
        val prefs = requireContext().getSharedPreferences("health_profile", android.content.Context.MODE_PRIVATE)
        val json = gson.toJson(dailyLogs)
        prefs.edit().putString("daily_logs", json).apply()
    }

    /**
     * Sets up profile information section
     */
    private fun setupProfileSection() {
        binding.buttonEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.buttonEditPhysicalMetrics.setOnClickListener {
            showEditPhysicalMetricsDialog()
        }
    }

    /**
     * Sets up daily metrics section
     */
    private fun setupDailyMetricsSection() {
        binding.buttonLogToday.setOnClickListener {
            showDailyMetricsDialog()
        }

        binding.buttonViewHistory.setOnClickListener {
            showHealthHistoryDialog()
        }
    }

    /**
     * Sets up vitals section
     */
    private fun setupVitalsSection() {
        binding.buttonUpdateVitals.setOnClickListener {
            showUpdateVitalsDialog()
        }
    }

    /**
     * Sets up medical information section
     */
    private fun setupMedicalInfoSection() {
        binding.buttonEditMedicalInfo.setOnClickListener {
            showEditMedicalInfoDialog()
        }
    }

    /**
     * Displays profile data in UI
     */
    private fun displayProfileData() {
        // Personal Info
        binding.textViewUserName.text = healthProfile.fullName.ifEmpty { "Set your name" }
        binding.textViewAgeGender.text = if (healthProfile.age > 0 && healthProfile.gender.isNotEmpty()) {
            "${healthProfile.age} years • ${healthProfile.gender}"
        } else {
            "Age & Gender not set"
        }
        binding.textViewBloodGroup.text = healthProfile.bloodGroup.ifEmpty { "Not set" }

        // Physical Metrics
        binding.textViewHeight.text = if (healthProfile.height > 0) "${healthProfile.height} cm" else "Not set"
        binding.textViewWeight.text = if (healthProfile.weight > 0) "${healthProfile.weight} kg" else "Not set"

        // BMI
        val bmi = healthProfile.calculateBMI()
        if (bmi > 0) {
            binding.textViewBMI.text = String.format("%.1f", bmi)
            binding.textViewBMICategory.text = healthProfile.getBMICategory()
            binding.progressBarBMI.progress = ((bmi / 40.0) * 100).toInt().coerceIn(0, 100)
        } else {
            binding.textViewBMI.text = "--"
            binding.textViewBMICategory.text = "Calculate BMI"
        }

        // Daily Metrics
        binding.textViewDailySteps.text = "${healthProfile.dailySteps} steps"
        binding.textViewWaterIntake.text = String.format("%.1fL / %.1fL", healthProfile.waterIntake, healthProfile.getDailyWaterGoal())
        binding.progressBarWater.progress = healthProfile.getWaterIntakePercentage()
        binding.textViewSleepHours.text = String.format("%.1f hours", healthProfile.sleepHours)
        binding.textViewExercise.text = "${healthProfile.exerciseMinutes} mins"

        // Vitals
        if (healthProfile.bloodPressureSystolic > 0) {
            binding.textViewBloodPressure.text = "${healthProfile.bloodPressureSystolic}/${healthProfile.bloodPressureDiastolic} mmHg"
            binding.textViewBPStatus.text = healthProfile.getBloodPressureCategory()
        } else {
            binding.textViewBloodPressure.text = "Not recorded"
            binding.textViewBPStatus.text = ""
        }

        if (healthProfile.bloodSugar > 0) {
            binding.textViewBloodSugar.text = "${healthProfile.bloodSugar} mg/dL"
            binding.textViewSugarStatus.text = healthProfile.getBloodSugarStatus()
        } else {
            binding.textViewBloodSugar.text = "Not recorded"
            binding.textViewSugarStatus.text = ""
        }

        if (healthProfile.heartRate > 0) {
            binding.textViewHeartRate.text = "${healthProfile.heartRate} bpm"
            binding.textViewHeartRateStatus.text = healthProfile.getHeartRateStatus()
        } else {
            binding.textViewHeartRate.text = "Not recorded"
            binding.textViewHeartRateStatus.text = ""
        }

        // Medical Info
        binding.textViewAllergies.text = healthProfile.allergies.ifEmpty { "None recorded" }
        binding.textViewMedications.text = healthProfile.medications.ifEmpty { "None recorded" }
        binding.textViewEmergencyContact.text = if (healthProfile.emergencyContact.isNotEmpty()) {
            "${healthProfile.emergencyContact}\n${healthProfile.emergencyPhone}"
        } else {
            "Not set"
        }

        // Profile Completion
        val completion = healthProfile.getProfileCompletionPercentage()
        binding.progressBarProfileCompletion.progress = completion
        binding.textViewProfileCompletion.text = "$completion% Complete"

        // Last Updated
        binding.textViewLastUpdated.text = "Last updated: ${healthProfile.getFormattedLastUpdated()}"
    }

    /**
     * Updates health insights and recommendations
     */
    private fun updateHealthInsights() {
        val insights = mutableListOf<String>()

        // BMI insights
        val bmi = healthProfile.calculateBMI()
        if (bmi > 0) {
            when (healthProfile.getBMICategory()) {
                "Underweight" -> insights.add("💡 Your BMI indicates you're underweight. Consider consulting a nutritionist.")
                "Overweight" -> insights.add("💡 Your BMI suggests you're overweight. Regular exercise and balanced diet recommended.")
                "Obese" -> insights.add("⚠️ Your BMI indicates obesity. Please consult a healthcare professional.")
                else -> insights.add("✅ Your BMI is in the healthy range! Keep it up!")
            }
        }

        // Water intake
        val waterPercentage = healthProfile.getWaterIntakePercentage()
        if (waterPercentage < 50) {
            insights.add("💧 You're not drinking enough water. Try to increase your intake!")
        } else if (waterPercentage >= 100) {
            insights.add("💧 Great hydration! You've met your daily water goal!")
        }

        // Sleep
        if (healthProfile.sleepHours > 0) {
            if (healthProfile.sleepHours < 6) {
                insights.add("😴 You're not getting enough sleep. Aim for 7-9 hours per night.")
            } else if (healthProfile.sleepHours >= 7 && healthProfile.sleepHours <= 9) {
                insights.add("😴 Excellent sleep duration! Keep maintaining this routine.")
            }
        }

        // Blood pressure
        if (healthProfile.bloodPressureSystolic > 0) {
            when (healthProfile.getBloodPressureCategory()) {
                "High Stage 1", "High Stage 2" -> insights.add("⚠️ Your blood pressure is elevated. Please consult a doctor.")
                "Normal" -> insights.add("❤️ Your blood pressure is in the healthy range!")
            }
        }

        // Display insights
        if (insights.isNotEmpty()) {
            binding.textViewHealthInsights.text = insights.joinToString("\n\n")
            binding.cardHealthInsights.visibility = View.VISIBLE
        } else {
            binding.cardHealthInsights.visibility = View.GONE
        }
    }

    /**
     * Shows edit profile dialog
     */
    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val editName = dialogView.findViewById<EditText>(R.id.editTextName)
        val editAge = dialogView.findViewById<EditText>(R.id.editTextAge)
        val editGender = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerGender)
        val editBloodGroup = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerBloodGroup)

        // Set current values
        editName.setText(healthProfile.fullName)
        editAge.setText(if (healthProfile.age > 0) healthProfile.age.toString() else "")

        // Setup spinners
        val genders = arrayOf("Select Gender", "Male", "Female", "Other")
        editGender.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genders)
        editGender.setSelection(genders.indexOf(healthProfile.gender).coerceAtLeast(0))

        val bloodGroups = arrayOf("Select Blood Group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        editBloodGroup.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bloodGroups)
        editBloodGroup.setSelection(bloodGroups.indexOf(healthProfile.bloodGroup).coerceAtLeast(0))

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Personal Information")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                healthProfile.fullName = editName.text.toString()
                healthProfile.age = editAge.text.toString().toIntOrNull() ?: 0
                healthProfile.gender = if (editGender.selectedItemPosition > 0) editGender.selectedItem.toString() else ""
                healthProfile.bloodGroup = if (editBloodGroup.selectedItemPosition > 0) editBloodGroup.selectedItem.toString() else ""

                saveHealthProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows edit physical metrics dialog
     */
    private fun showEditPhysicalMetricsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_physical_metrics, null)
        val editHeight = dialogView.findViewById<EditText>(R.id.editTextHeight)
        val editWeight = dialogView.findViewById<EditText>(R.id.editTextWeight)
        val editTargetWeight = dialogView.findViewById<EditText>(R.id.editTextTargetWeight)

        // Set current values
        editHeight.setText(if (healthProfile.height > 0) healthProfile.height.toString() else "")
        editWeight.setText(if (healthProfile.weight > 0) healthProfile.weight.toString() else "")
        editTargetWeight.setText(if (healthProfile.targetWeight > 0) healthProfile.targetWeight.toString() else "")

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Physical Metrics")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                healthProfile.height = editHeight.text.toString().toDoubleOrNull() ?: 0.0
                healthProfile.weight = editWeight.text.toString().toDoubleOrNull() ?: 0.0
                healthProfile.targetWeight = editTargetWeight.text.toString().toDoubleOrNull() ?: 0.0

                saveHealthProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows daily metrics dialog for logging today's health data
     */
    private fun showDailyMetricsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_daily_metrics, null)
        val editSteps = dialogView.findViewById<EditText>(R.id.editTextSteps)
        val editWater = dialogView.findViewById<EditText>(R.id.editTextWater)
        val editSleep = dialogView.findViewById<EditText>(R.id.editTextSleep)
        val editExercise = dialogView.findViewById<EditText>(R.id.editTextExercise)

        // Pre-fill with current day's values
        editSteps.setText(if (healthProfile.dailySteps > 0) healthProfile.dailySteps.toString() else "")
        editWater.setText(if (healthProfile.waterIntake > 0) healthProfile.waterIntake.toString() else "")
        editSleep.setText(if (healthProfile.sleepHours > 0) healthProfile.sleepHours.toString() else "")
        editExercise.setText(if (healthProfile.exerciseMinutes > 0) healthProfile.exerciseMinutes.toString() else "")

        AlertDialog.Builder(requireContext())
            .setTitle("Log Today's Metrics")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                healthProfile.dailySteps = editSteps.text.toString().toIntOrNull() ?: 0
                healthProfile.waterIntake = editWater.text.toString().toDoubleOrNull() ?: 0.0
                healthProfile.sleepHours = editSleep.text.toString().toDoubleOrNull() ?: 0.0
                healthProfile.exerciseMinutes = editExercise.text.toString().toIntOrNull() ?: 0

                // Create daily log entry
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val log = DailyHealthLog(
                    date = today,
                    weight = healthProfile.weight,
                    bloodPressureSystolic = healthProfile.bloodPressureSystolic,
                    bloodPressureDiastolic = healthProfile.bloodPressureDiastolic,
                    bloodSugar = healthProfile.bloodSugar,
                    heartRate = healthProfile.heartRate,
                    dailySteps = healthProfile.dailySteps,
                    waterIntake = healthProfile.waterIntake,
                    sleepHours = healthProfile.sleepHours,
                    exerciseMinutes = healthProfile.exerciseMinutes
                )

                // Remove today's entry if exists and add new one
                dailyLogs.removeAll { it.date == today }
                dailyLogs.add(0, log)
                saveDailyLogs()

                saveHealthProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows update vitals dialog
     */
    private fun showUpdateVitalsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_vitals, null)
        val editBPSystolic = dialogView.findViewById<EditText>(R.id.editTextBPSystolic)
        val editBPDiastolic = dialogView.findViewById<EditText>(R.id.editTextBPDiastolic)
        val editBloodSugar = dialogView.findViewById<EditText>(R.id.editTextBloodSugar)
        val editHeartRate = dialogView.findViewById<EditText>(R.id.editTextHeartRate)
        val editTemperature = dialogView.findViewById<EditText>(R.id.editTextTemperature)

        // Set current values
        editBPSystolic.setText(if (healthProfile.bloodPressureSystolic > 0) healthProfile.bloodPressureSystolic.toString() else "")
        editBPDiastolic.setText(if (healthProfile.bloodPressureDiastolic > 0) healthProfile.bloodPressureDiastolic.toString() else "")
        editBloodSugar.setText(if (healthProfile.bloodSugar > 0) healthProfile.bloodSugar.toString() else "")
        editHeartRate.setText(if (healthProfile.heartRate > 0) healthProfile.heartRate.toString() else "")
        editTemperature.setText(if (healthProfile.bodyTemperature > 0) healthProfile.bodyTemperature.toString() else "")

        AlertDialog.Builder(requireContext())
            .setTitle("Update Vital Signs")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                healthProfile.bloodPressureSystolic = editBPSystolic.text.toString().toIntOrNull() ?: 0
                healthProfile.bloodPressureDiastolic = editBPDiastolic.text.toString().toIntOrNull() ?: 0
                healthProfile.bloodSugar = editBloodSugar.text.toString().toDoubleOrNull() ?: 0.0
                healthProfile.heartRate = editHeartRate.text.toString().toIntOrNull() ?: 0
                healthProfile.bodyTemperature = editTemperature.text.toString().toDoubleOrNull() ?: 0.0

                saveHealthProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows edit medical information dialog
     */
    private fun showEditMedicalInfoDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_medical_info, null)
        val editAllergies = dialogView.findViewById<EditText>(R.id.editTextAllergies)
        val editMedications = dialogView.findViewById<EditText>(R.id.editTextMedications)
        val editConditions = dialogView.findViewById<EditText>(R.id.editTextConditions)
        val editEmergencyContact = dialogView.findViewById<EditText>(R.id.editTextEmergencyContact)
        val editEmergencyPhone = dialogView.findViewById<EditText>(R.id.editTextEmergencyPhone)

        // Set current values
        editAllergies.setText(healthProfile.allergies)
        editMedications.setText(healthProfile.medications)
        editConditions.setText(healthProfile.medicalConditions)
        editEmergencyContact.setText(healthProfile.emergencyContact)
        editEmergencyPhone.setText(healthProfile.emergencyPhone)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Medical Information")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                healthProfile.allergies = editAllergies.text.toString()
                healthProfile.medications = editMedications.text.toString()
                healthProfile.medicalConditions = editConditions.text.toString()
                healthProfile.emergencyContact = editEmergencyContact.text.toString()
                healthProfile.emergencyPhone = editEmergencyPhone.text.toString()

                saveHealthProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows health history dialog
     */
    private fun showHealthHistoryDialog() {
        if (dailyLogs.isEmpty()) {
            showMessage("No health history available yet")
            return
        }

        val historyText = StringBuilder()
        dailyLogs.take(7).forEach { log ->
            historyText.append("📅 ${log.getFormattedDate()}\n")
            if (log.weight > 0) historyText.append("⚖️ Weight: ${log.weight} kg\n")
            if (log.bloodPressureSystolic > 0) historyText.append("💓 BP: ${log.bloodPressureSystolic}/${log.bloodPressureDiastolic} mmHg\n")
            if (log.bloodSugar > 0) historyText.append("🩸 Blood Sugar: ${log.bloodSugar} mg/dL\n")
            if (log.dailySteps > 0) historyText.append("👣 Steps: ${log.dailySteps}\n")
            if (log.waterIntake > 0) historyText.append("💧 Water: ${log.waterIntake}L\n")
            if (log.sleepHours > 0) historyText.append("😴 Sleep: ${log.sleepHours}hrs\n")
            historyText.append("\n")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Health History (Last 7 Days)")
            .setMessage(historyText.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Shows a message to the user
     */
    private fun showMessage(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}