package com.example.diarybook.broadcast

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.diarybook.R
import com.example.diarybook.constant.Constant.channelID
import com.example.diarybook.constant.Constant.notificationDescription
import com.example.diarybook.constant.Constant.notificationChannelName
import com.example.diarybook.constant.Constant.notificationMessage
import com.example.diarybook.constant.Constant.notificationId
import com.example.diarybook.constant.Constant.notificationTitle
import com.example.diarybook.view.dialog.NotificationDialog
import java.util.*



class Notification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.applicationlogo)
            .setContentTitle(intent.getStringExtra(notificationTitle))
            .setContentText(intent.getStringExtra(notificationMessage))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

     fun scheduleNotification(
        activity: Activity, title: String, message: String,
        minute: Int, hour: Int, day: Int,
        month: Int, year: Int
    ) {
        val intent = Intent(activity.applicationContext, Notification::class.java)
        intent.putExtra(notificationTitle, title)
        intent.putExtra(notificationMessage, message)

        val pendingIntent = PendingIntent.getBroadcast(
            activity.applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val notificationTime = getNotificationTime(minute, hour, day, month, year)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )

        val notificationDialog = NotificationDialog(activity)

        notificationDialog.getNotificationDialog(activity, notificationTime, title, message)
    }

    fun createNotificationChannel(activity: Activity) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, notificationChannelName, importance)
        channel.description = notificationDescription
        val notificationManager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getNotificationTime(minute: Int, hour: Int, day: Int, month: Int, year: Int): Long {

        val notificationCalendar = Calendar.getInstance()
        notificationCalendar.set(year, month, day, hour, minute)

        return notificationCalendar.timeInMillis
    }
}