package com.notifyvault.app
data class NotificationRecord(
    val id: Long,
    val packageName: String,
    val appName: String,
    val title: String,
    val message: String,
    val postedAt: Long,
    val removedAt: Long?
)
