package com.notifyvault.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotificationDb(context: Context) : SQLiteOpenHelper(context, "notifyvault.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, package_name TEXT NOT NULL, app_name TEXT NOT NULL, title TEXT, message TEXT, posted_at INTEGER NOT NULL, removed_at INTEGER, notification_key TEXT)")
        db.execSQL("CREATE INDEX idx_posted ON notifications(posted_at DESC)")
        db.execSQL("CREATE INDEX idx_package ON notifications(package_name)")
        db.execSQL("CREATE INDEX idx_key ON notifications(notification_key)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    @Synchronized fun insert(packageName: String, appName: String, title: String, message: String, postedAt: Long, key: String?) {
        val v = ContentValues()
        v.put("package_name", packageName); v.put("app_name", appName); v.put("title", title)
        v.put("message", message); v.put("posted_at", postedAt); v.put("notification_key", key)
        writableDatabase.insert("notifications", null, v)
    }
    @Synchronized fun markRemoved(key: String?, removedAt: Long) {
        if (key.isNullOrBlank()) return
        val v = ContentValues(); v.put("removed_at", removedAt)
        writableDatabase.update("notifications", v, "notification_key = ? AND removed_at IS NULL", arrayOf(key))
    }
    fun search(query: String): List<NotificationRecord> {
        val result = mutableListOf<NotificationRecord>()
        val clean = query.trim()
        val selection = if (clean.isEmpty()) null else "app_name LIKE ? OR title LIKE ? OR message LIKE ?"
        val args = if (clean.isEmpty()) null else {
            val q = "%" + clean + "%"; arrayOf(q, q, q)
        }
        readableDatabase.query("notifications", null, selection, args, null, null, "posted_at DESC", "500").use { c ->
            while (c.moveToNext()) {
                val r = c.getColumnIndexOrThrow("removed_at")
                result.add(NotificationRecord(
                    c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("package_name")),
                    c.getString(c.getColumnIndexOrThrow("app_name")),
                    c.getString(c.getColumnIndexOrThrow("title")) ?: "",
                    c.getString(c.getColumnIndexOrThrow("message")) ?: "",
                    c.getLong(c.getColumnIndexOrThrow("posted_at")),
                    if (c.isNull(r)) null else c.getLong(r)
                ))
            }
        }
        return result
    }
    fun count(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM notifications", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    fun clearAll() { writableDatabase.delete("notifications", null, null) }
}
