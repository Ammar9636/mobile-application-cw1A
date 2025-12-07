package com.example.labexam3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Helper class for scheduling and managing hydration reminder alarms
 * Uses AlarmManager to set up recurring notifications
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferenceRepository = PreferenceRepository(context)

    companion object {
        private const val REQUEST_CODE_REMINDER = 2001
        private const val REQUEST_CODE_SNOOZE = 2002
        private const val SNOOZE_DURATION_MINUTES = 15
    }
    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    /**
     * Schedules the next hydration reminder based on user preferences
     */
    fun scheduleReminder() {
        if (!preferenceRepository.isHydrationEnabled()) {
            return
        }

        val intervalMinutes = preferenceRepository.getHydrationInterval()
        val intervalMillis = intervalMinutes * 60 * 1000L

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            // Add action to distinguish from other intents
            action = "HYDRATION_REMINDER_ACTION"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + intervalMillis

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (canScheduleExactAlarms()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                // Fallback: Use inexact repeating alarm
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + intervalMillis,
                    intervalMillis,
                    pendingIntent
                )
            }

        } catch (e: SecurityException) {
            // Final fallback: Use set() method
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Cancels all scheduled hydration reminders
     */
    fun cancelReminder() {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * Schedules a snooze reminder (15 minutes from now)
     */
    fun scheduleSnoozeReminder() {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SNOOZE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = System.currentTimeMillis() + (SNOOZE_DURATION_MINUTES * 60 * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fall back to inexact alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
            )
        }
    }

    /**
     * Sends a test reminder notification immediately
     */
    fun sendTestReminder() {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_IS_TEST, true)
        }
        context.sendBroadcast(intent)
    }

    /**
     * Reschedules the reminder with a new interval
     * Cancels existing reminder and sets up a new one
     */
    fun rescheduleReminder() {
        cancelReminder()
        if (preferenceRepository.isHydrationEnabled()) {
            scheduleReminder()
        }
    }

    /**
     * Checks if reminders are currently scheduled
     * @return true if reminders are scheduled, false otherwise
     */
    fun areRemindersScheduled(): Boolean {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    /**
     * Gets the next scheduled reminder time
     * @return timestamp of next reminder or -1 if none scheduled
     */
    fun getNextReminderTime(): Long {
        if (!preferenceRepository.isHydrationEnabled()) {
            return -1
        }

        // This is an approximation since we can't directly query AlarmManager
        val intervalMinutes = preferenceRepository.getHydrationInterval()
        val intervalMillis = intervalMinutes * 60 * 1000L
        return System.currentTimeMillis() + intervalMillis
    }

    /**
     * Gets a human-readable string for the next reminder time
     * @return formatted string or "Not scheduled" if none
     */
    fun getNextReminderTimeString(): String {
        val nextTime = getNextReminderTime()
        if (nextTime == -1L) {
            return "Not scheduled"
        }

        val intervalMinutes = preferenceRepository.getHydrationInterval()
        return when {
            intervalMinutes < 60 -> "In $intervalMinutes minutes"
            intervalMinutes == 60 -> "In 1 hour"
            intervalMinutes % 60 == 0 -> "In ${intervalMinutes / 60} hours"
            else -> {
                val hours = intervalMinutes / 60
                val minutes = intervalMinutes % 60
                "In $hours hours and $minutes minutes"
            }
        }
    }
}