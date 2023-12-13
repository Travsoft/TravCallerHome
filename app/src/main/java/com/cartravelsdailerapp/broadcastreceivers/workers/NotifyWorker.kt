package com.cartravelsdailerapp.broadcastreceivers.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R

class NotifyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "NotificationChannel"
        const val CHANNEL_NAME = "Notification"
    }

    private val mContext = context

    override fun doWork(): Result {
       // triggerNotification()
        return Result.success()
    }

/*
    private fun triggerNotification() {
        val notificationManager =
            mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(mContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            mContext,
            1,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.arrow_blue)
            .setContentTitle("incoming call")
            .setContentText(inputData.getString(PrefUtils.ContactNumber))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)

        notification.setContentIntent(pendingIntent)
        notificationManager.notify(1, notification.build())
    }
*/
}