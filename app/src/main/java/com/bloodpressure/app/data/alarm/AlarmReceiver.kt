package com.bloodpressure.app.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.bloodpressure.app.ALARM") {
            val title = intent.getStringExtra("title") ?: "测量血压提醒"
            val message = intent.getStringExtra("message") ?: "该测量血压了"
            
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone?.play()
            
            val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            openAppIntent?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                openAppIntent?.let { context.startActivity(it) }
            } else {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                val appTasks = activityManager.appTasks
                if (appTasks.isNotEmpty()) {
                    appTasks.first().setExcludeFromRecents(false)
                }
                openAppIntent?.let { context.startActivity(it) }
            }
        }
    }
}
