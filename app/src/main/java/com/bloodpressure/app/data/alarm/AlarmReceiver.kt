package com.bloodpressure.app.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.MainActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "blood_pressure_reminder"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val logMsg = "onReceive called, action: ${intent.action}"
        writeToLogFile(context, logMsg)
        Log.d(TAG, logMsg)
        
        try {
            if (intent.action == "com.bloodpressure.app.ALARM") {
                val title = intent.getStringExtra("title") ?: "测量血压提醒"
                val message = intent.getStringExtra("message") ?: "该测量血压了"
                
                val logMsg2 = "Alarm triggered: $title - $message"
                writeToLogFile(context, logMsg2)
                Log.d(TAG, logMsg2)
                
                createNotificationChannel(context)
                showNotification(context, title, message)
                
                try {
                    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    
                    val logMsg3 = "Playing ringtone from: $alarmUri"
                    writeToLogFile(context, logMsg3)
                    Log.d(TAG, logMsg3)
                    
                    val ringtone = RingtoneManager.getRingtone(context, alarmUri)
                    ringtone?.play()
                    val logMsg4 = "Ringtone playing"
                    writeToLogFile(context, logMsg4)
                    Log.d(TAG, logMsg4)
                } catch (e: Exception) {
                    val logMsg5 = "Error playing ringtone: ${e.message}"
                    writeToLogFile(context, logMsg5)
                    Log.e(TAG, logMsg5, e)
                }
            }
        } catch (e: Exception) {
            val logMsg6 = "Error in onReceive: ${e.message}"
            writeToLogFile(context, logMsg6)
            Log.e(TAG, logMsg6, e)
        }
    }

    private fun writeToLogFile(context: Context, message: String) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val logLine = "$timestamp - $message\n"
            
            val logFile = File(context.filesDir, "alarm_log.txt")
            logFile.appendText(logLine)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "血压提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "测量血压的提醒通知"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        Log.d(TAG, "Creating notification: $title")
        
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Notification sent")
    }
}
