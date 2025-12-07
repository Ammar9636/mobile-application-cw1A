package com.example.labexam3

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing user's health profile and daily health metrics
 */
data class HealthProfile(
    // Personal Information
    var fullName: String = "",
    var age: Int = 0,
    var gender: String = "", // Male, Female, Other
    var bloodGroup: String = "", // A+, A-, B+, B-, AB+, AB-, O+, O-

    // Physical Metrics
    var height: Double = 0.0, // in cm
    var weight: Double = 0.0, // in kg
    var targetWeight: Double = 0.0, // Goal weight in kg

    // Health Vitals
    var bloodPressureSystolic: Int = 0, // Upper number
    var bloodPressureDiastolic: Int = 0, // Lower number
    var bloodSugar: Double = 0.0, // mg/dL
    var heartRate: Int = 0, // bpm
    var bodyTemperature: Double = 0.0, // Celsius

    // Activity Metrics
    var dailySteps: Int = 0,
    var waterIntake: Double = 0.0, // in liters
    var sleepHours: Double = 0.0, // hours
    var exerciseMinutes: Int = 0, // minutes

    // Medical Information
    var allergies: String = "",
    var medications: String = "",
    var medicalConditions: String = "",
    var emergencyContact: String = "",
    var emergencyPhone: String = "",

    // Tracking
    val lastUpdated: Long = System.currentTimeMillis(),
    val userId: String = "" // For multi-user support
) {

    /**
     * Calculates BMI (Body Mass Index)
     * Formula: weight(kg) / (height(m))^2
     */
    fun calculateBMI(): Double {
        if (height <= 0 || weight <= 0) return 0.0
        val heightInMeters = height / 100.0
        return weight / (heightInMeters * heightInMeters)
    }

    /**
     * Gets BMI category
     */
    fun getBMICategory(): String {
        val bmi = calculateBMI()
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    /**
     * Gets blood pressure category
     */
    fun getBloodPressureCategory(): String {
        return when {
            bloodPressureSystolic < 120 && bloodPressureDiastolic < 80 -> "Normal"
            bloodPressureSystolic < 130 && bloodPressureDiastolic < 80 -> "Elevated"
            bloodPressureSystolic < 140 || bloodPressureDiastolic < 90 -> "High Stage 1"
            bloodPressureSystolic >= 140 || bloodPressureDiastolic >= 90 -> "High Stage 2"
            else -> "Unknown"
        }
    }

    /**
     * Checks if blood sugar is in normal range (fasting)
     */
    fun getBloodSugarStatus(): String {
        return when {
            bloodSugar < 70 -> "Low"
            bloodSugar <= 100 -> "Normal"
            bloodSugar <= 125 -> "Pre-diabetic"
            else -> "High"
        }
    }

    /**
     * Gets heart rate category
     */
    fun getHeartRateStatus(): String {
        return when {
            heartRate < 60 -> "Low (Bradycardia)"
            heartRate <= 100 -> "Normal"
            else -> "High (Tachycardia)"
        }
    }

    /**
     * Calculates daily water intake goal based on weight
     * Recommendation: 30-35ml per kg of body weight
     */
    fun getDailyWaterGoal(): Double {
        if (weight <= 0) return 2.0 // Default 2 liters
        return (weight * 0.033) // 33ml per kg
    }

    /**
     * Gets water intake percentage
     */
    fun getWaterIntakePercentage(): Int {
        val goal = getDailyWaterGoal()
        if (goal <= 0) return 0
        return ((waterIntake / goal) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Calculates calories burned based on steps
     * Rough estimate: 0.04 calories per step
     */
    fun getEstimatedCaloriesBurned(): Int {
        return (dailySteps * 0.04).toInt()
    }

    /**
     * Gets formatted last updated date
     */
    fun getFormattedLastUpdated(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return formatter.format(Date(lastUpdated))
    }

    /**
     * Validates if essential health data is complete
     */
    fun isProfileComplete(): Boolean {
        return fullName.isNotEmpty() &&
                age > 0 &&
                height > 0 &&
                weight > 0 &&
                bloodGroup.isNotEmpty()
    }

    /**
     * Gets completion percentage
     */
    fun getProfileCompletionPercentage(): Int {
        var completed = 0
        var total = 15 // Total fields to track

        if (fullName.isNotEmpty()) completed++
        if (age > 0) completed++
        if (gender.isNotEmpty()) completed++
        if (bloodGroup.isNotEmpty()) completed++
        if (height > 0) completed++
        if (weight > 0) completed++
        if (bloodPressureSystolic > 0) completed++
        if (bloodPressureDiastolic > 0) completed++
        if (bloodSugar > 0) completed++
        if (heartRate > 0) completed++
        if (dailySteps > 0) completed++
        if (waterIntake > 0) completed++
        if (sleepHours > 0) completed++
        if (emergencyContact.isNotEmpty()) completed++
        if (emergencyPhone.isNotEmpty()) completed++

        return (completed * 100) / total
    }
}

/**
 * Daily health log entry for tracking daily metrics
 */
data class DailyHealthLog(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // yyyy-MM-dd format
    val weight: Double = 0.0,
    val bloodPressureSystolic: Int = 0,
    val bloodPressureDiastolic: Int = 0,
    val bloodSugar: Double = 0.0,
    val heartRate: Int = 0,
    val dailySteps: Int = 0,
    val waterIntake: Double = 0.0,
    val sleepHours: Double = 0.0,
    val exerciseMinutes: Int = 0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Gets formatted date for display
     */
    fun getFormattedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return try {
            val date = inputFormat.parse(date)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            date
        }
    }
}