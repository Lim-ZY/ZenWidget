package com.example.zenwidget

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

object DndManager {

    /**
     * Checks if the user has granted Notification Policy access to the app.
     */
    fun hasPermission(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Directs the user to the Android System Settings page to grant access.
     */
    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Toggles Do Not Disturb Mode.
     * @param enable True to turn DND on (Priority Only), False to turn it off (All notifications).
     */
    fun setDoNotDisturb(context: Context, enable: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.isNotificationPolicyAccessGranted) {
            val filter = if (enable) {
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            } else {
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
            notificationManager.setInterruptionFilter(filter)
        }
    }
}