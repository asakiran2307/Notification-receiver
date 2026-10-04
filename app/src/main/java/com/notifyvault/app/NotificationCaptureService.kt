package com.notifyvault.app

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationCaptureService : NotificationListenerService() {
    private lateinit var db: NotificationDb

    override fun onCreate() {
        super.onCreate()
        db = NotificationDb(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val e = sbn.notification.extras
        val title = e.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = e.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val big = e.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val message = if (big.isNotBlank()) big else text
        if (title.isBlank() && message.isBlank()) return

        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, PackageManager.GET_META_DATA)
            ).toString()
        } catch (_: Exception) {
            sbn.packageName
        }

        db.insert(sbn.packageName, appName, title, message, sbn.postTime, sbn.key)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        db.markRemoved(sbn.key, System.currentTimeMillis())
    }
}
