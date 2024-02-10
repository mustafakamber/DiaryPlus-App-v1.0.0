package com.example.diarybook.view.sheet

import android.app.*
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.example.diarybook.R
import com.example.diarybook.broadcast.Notification
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout

class NotificationSheet(private val context: Context) {
    private lateinit var notificationBottomSheet: BottomSheetDialog

    val notification = Notification()

    fun getNotificationSheet(activity: Activity) {

        notificationBottomSheet = BottomSheetDialog(
            context,
            R.style.BottomSheetDialogTheme
        )

        val bottomSheetView = LayoutInflater.from(context).inflate(
            R.layout.bottom_sheet_notifications,
            null
        )

        val notificationTitleText =
            bottomSheetView.findViewById<EditText>(R.id.notificationTitleText)
        val notificationTitleLayout =
            bottomSheetView.findViewById<TextInputLayout>(R.id.notificationTitleLayout)
        val notificationMessageText =
            bottomSheetView.findViewById<EditText>(R.id.notificationMessageText)
        val notificationMessageLayout =
            bottomSheetView.findViewById<TextInputLayout>(R.id.notificationMessageLayout)
        val notificationDatePicker =
            bottomSheetView.findViewById<DatePicker>(R.id.notificationsDatePicker)
        val notificationTimePicker =
            bottomSheetView.findViewById<TimePicker>(R.id.notificationsTimePicker)
        val notificationSubmitButton =
            bottomSheetView.findViewById<Button>(R.id.notificationsSubmitButton)
        val notificationBackButton =
            bottomSheetView.findViewById<ImageView>(R.id.notificationBackButton)

        val title = notificationTitleText.text.toString()
        val message = notificationMessageText.text.toString()

        notificationSubmitButton.setOnClickListener {
            if (TextUtils.isEmpty(title) ||
                TextUtils.isEmpty(message)
            ) {
                if (TextUtils.isEmpty(title)) {
                    notificationTitleLayout.isEndIconVisible = false
                    notificationTitleText.error =
                        context.getString(R.string.enter_notify_title_message)
                    notificationTitleText.addTextChangedListener {
                        notificationTitleLayout.isEndIconVisible = true
                    }
                }
                if (TextUtils.isEmpty(message)) {
                    notificationMessageLayout.isEndIconVisible = false
                    notificationMessageText.error = context.getString(R.string.enter_notify_message)
                    notificationMessageText.addTextChangedListener {
                        notificationMessageLayout.isEndIconVisible = true
                    }
                }
                return@setOnClickListener
            } else {
                notification.scheduleNotification(
                    activity,
                    notificationTitleText.text.toString().trim(),
                    notificationMessageText.text.toString().trim(),
                    notificationTimePicker.minute,
                    notificationTimePicker.hour,
                    notificationDatePicker.dayOfMonth,
                    notificationDatePicker.month,
                    notificationDatePicker.year
                )
            }
        }

        notificationBackButton.setOnClickListener {
            notificationBottomSheet?.dismiss()
            return@setOnClickListener
        }

        notificationBottomSheet?.setContentView(bottomSheetView)
        notificationBottomSheet?.show()
    }


}