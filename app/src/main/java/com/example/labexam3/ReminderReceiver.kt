package com.example.labexam3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * BroadcastReceiver for handling hydration reminder alarms
 * Creates and displays hydration reminder notifications
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "hydration_reminders"
        const val EXTRA_IS_TEST = "is_test_reminder"

        // Notification messages
        private val REMINDER_MESSAGES = listOf(
            "💧 Time to drink water! Stay hydrated!",
            "🥤 Don't forget to hydrate! Your body needs water.",
            "💙 Hydration break! Drink a glass of water now.",
            "🌊 Keep the flow going! Time for some H2O.",
            "💧 Water reminder: Stay healthy, stay hydrated!",
            "🚰 Your body is calling for water! Time to drink up.",
            "💦 Hydration checkpoint! Grab that water bottle.",
            "🧊 Cool and refreshing water awaits! Time to drink."
        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        val isTestReminder = intent?.getBooleanExtra(EXTRA_IS_TEST, false) ?: false

        // Check if hydration reminders are still enabled
        val preferenceRepository = PreferenceRepository(context)
        if (!preferenceRepository.isHydrationEnabled() && !isTestReminder) {
            return
        }

        // Create notification channel (for API 26+)
        createNotificationChannel(context)

        // Show notification
        showHydrationNotification(context, isTestReminder)

        // Schedule next reminder if not a test
        if (!isTestReminder) {
            val reminderScheduler = ReminderScheduler(context)
            reminderScheduler.scheduleReminder()
        }
    }

    /**
     * Creates the notification channel for hydration reminders (Android 8.0+)
     * @param context Application context
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Notifications to remind you to drink water"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays the hydration reminder notification
     * @param context Application context
     * @param isTestReminder Whether this is a test reminder
     */
    private fun showHydrationNotification(context: Context, isTestReminder: Boolean) {
        // Create intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("source", "notification")
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification content
        val message = if (isTestReminder) {
            "🧪 Test reminder: This is how your hydration notifications will look!"
        } else {
            REMINDER_MESSAGES.random()
        }

        val title = if (isTestReminder) "Test Hydration Reminder" else "💧 Hydration Reminder"

        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 🔧 Use system icon temporarily
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 🔧 Changed to HIGH
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 🔧 Added defaults
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel, // 🔧 Use system icon
                "Mark as Done",
                createMarkAsDoneIntent(context)
            )
            .addAction(
                android.R.drawable.ic_menu_revert, // 🔧 Use system icon
                "Remind Later",
                createSnoozeIntent(context)
            )

        // Show notification
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Handle notification permission issues
                android.util.Log.e("ReminderReceiver", "Notification permission denied")
            }
        }
    }
    /**
     * Creates a PendingIntent for the "Mark as Done" action
     * @param context Application context
     * @return PendingIntent for mark as done action
     */
    private fun createMarkAsDoneIntent(context: Context): PendingIntent {
        val intent = Intent(context, HydrationActionReceiver::class.java).apply {
            action = "ACTION_MARK_DONE"
        }
        return PendingIntent.getBroadcast(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates a PendingIntent for the "Remind Later" action
     * @param context Application context
     * @return PendingIntent for snooze action
     */
    private fun createSnoozeIntent(context: Context): PendingIntent {
        val intent = Intent(context, HydrationActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
        }
        return PendingIntent.getBroadcast(
            context,
            1003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/**
 * Separate receiver for handling notification actions
 */
class HydrationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        when (intent?.action) {
            "ACTION_MARK_DONE" -> {
                // Dismiss the notification
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(ReminderReceiver.NOTIFICATION_ID)

                // You could also log this action or update statistics here
            }
            "ACTION_SNOOZE" -> {
                // Dismiss current notification
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(ReminderReceiver.NOTIFICATION_ID)

                // Schedule a reminder in 15 minutes
                val reminderScheduler = ReminderScheduler(context)
                reminderScheduler.scheduleSnoozeReminder()
            }
        }
    }
}