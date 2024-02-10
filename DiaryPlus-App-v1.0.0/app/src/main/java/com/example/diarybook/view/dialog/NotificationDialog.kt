package com.example.diarybook.view.dialog

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.diarybook.R
import java.util.*

class NotificationDialog
    (private var context: Context) {

    fun getNotificationDialog(
        activity: Activity, notificationTime: Long,
        title: String, message: String
    ) {
        val date = Date(notificationTime)
        val dateFormat =
            android.text.format.DateFormat.getLongDateFormat(activity.applicationContext)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(activity.applicationContext)

        val spaceString = " "

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.notification_scheduled))
            .setMessage(
                context.getString(R.string.notification_scheduled_title) + spaceString + title +
                        "\n" + context.getString(R.string.notification_scheduled_message) + spaceString + message +
                        "\n" + context.getString(R.string.notification_scheduled_time) + spaceString + dateFormat.format(
                    date
                ) + spaceString + timeFormat.format(date)
            )
            .setPositiveButton(context.getString(R.string.notification_scheduled_ok)) { _, _ -> }
            .show()
    }

}