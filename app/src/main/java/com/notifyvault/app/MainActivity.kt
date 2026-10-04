package com.notifyvault.app

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {
    private lateinit var db: NotificationDb
    private lateinit var status: TextView
    private lateinit var search: EditText
    private lateinit var list: LinearLayout

    override fun onCreate(state: Bundle?) {
        super.onCreate(state); db = NotificationDb(this); buildUi()
    }
    override fun onResume() { super.onResume(); if (::status.isInitialized) refresh() }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(28,24,28,20); setBackgroundColor(Color.rgb(16,16,20))
        }
        root.addView(TextView(this).apply { text="NotifyVault"; textSize=30f; setTextColor(Color.WHITE) })
        root.addView(TextView(this).apply { text="Private notification history • stored on this device"; textSize=14f; setTextColor(Color.LTGRAY) })
        status = TextView(this).apply { textSize=14f; setTextColor(Color.WHITE); setPadding(0,12,0,10) }; root.addView(status)
        root.addView(Button(this).apply {
            text="Enable Notification Access"
            setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        })
        search = EditText(this).apply {
            hint="Search app, title, or message"; setSingleLine(true); setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)
            setOnEditorActionListener { _,_,_ -> refresh(); true }
        }; root.addView(search)
        root.addView(Button(this).apply { text="Refresh History"; setOnClickListener { refresh() } })
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(0,10,0,40) }
        scroll.addView(list); root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        root.addView(Button(this).apply { text="Clear Local History"; setOnClickListener { db.clearAll(); refresh() } })
        setContentView(root); refresh()
    }

    private fun refresh() {
        status.text = if (isListenerEnabled()) "Capture is ON • " + db.count() + " saved notifications"
        else "Capture is OFF • grant Notification Access to start"
        list.removeAllViews()
        db.search(search.text?.toString().orEmpty()).forEach { item ->
            val time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.postedAt))
            val removed = if (item.removedAt != null) " • removed" else ""
            val card = TextView(this).apply {
                text = item.appName + " • " + time + removed + "\n" + item.title + "\n" + item.message
                textSize=15f; setTextColor(Color.WHITE); setPadding(18,16,18,16); setBackgroundColor(Color.rgb(31,31,38))
            }
            val p = LinearLayout.LayoutParams(-1,-2); p.setMargins(0,0,0,8); list.addView(card,p)
        }
    }
    private fun isListenerEnabled(): Boolean {
        val expected = ComponentName(this, NotificationCaptureService::class.java)
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return enabled.split(":").any { ComponentName.unflattenFromString(it) == expected }
    }
}
