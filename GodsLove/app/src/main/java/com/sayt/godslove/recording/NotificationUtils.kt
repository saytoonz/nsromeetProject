package com.sayt.godslove.recording

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.sayt.godslove.R

const val RECORDING_NOTIFICATION_CHANNEL_ID = "channel_recording"
const val FINISH_NOTIFICATION_CHANNEL_ID = "channel_finished"

@TargetApi(26)
fun Context.createNotificationChannels() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    val notificationChannels = ArrayList<NotificationChannel>()
    val recordingChannel = NotificationChannel(
        RECORDING_NOTIFICATION_CHANNEL_ID,
        getString(R.string.notification_channel_recording),
        NotificationManager.IMPORTANCE_HIGH
    )
    recordingChannel.enableLights(true)
    recordingChannel.lightColor = Color.RED
    recordingChannel.setShowBadge(false)
    recordingChannel.enableVibration(false)
    recordingChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    notificationChannels.add(recordingChannel)

    val finishChannel = NotificationChannel(
        FINISH_NOTIFICATION_CHANNEL_ID,
        getString(R.string.notification_channel_finish),
        NotificationManager.IMPORTANCE_DEFAULT
    )
    //finishChannel.enableLights(true)
    finishChannel.setShowBadge(true)
    finishChannel.enableVibration(false)
    finishChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    notificationChannels.add(finishChannel)

    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        .createNotificationChannels(notificationChannels)
}
