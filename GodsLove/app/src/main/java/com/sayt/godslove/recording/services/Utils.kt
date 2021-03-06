package com.sayt.godslove.recording.services

import android.media.MediaRecorder
import com.sayt.godslove.recording.data.DataManager
import com.sayt.godslove.recording.settings.PreferenceHelper

fun PreferenceHelper.generateOptions(dataManager: DataManager): Options? {
    val saveUri = saveLocation ?: return null
    val folderUri = saveUri.uri

    val uri = dataManager
        .create(folderUri, filename, "video/mp4", null) ?: return null

    return Options(
        video = VideoOptions(
            resolution = resolution.run {
                Resolution(first, second)
            },
            bitrate = videoBitrate,
            encoder = videoEncoder,
            fps = fps,
            virtualDisplayDpi = displayMetrics.densityDpi
        ),
        audio = if (recordAudio) {
            AudioOptions.RecordAudio(
                source = MediaRecorder.AudioSource.MIC,
                samplingRate = audioSamplingRate,
                encoder = audioEncoder,
                bitRate = audioBitrate
            )
        } else AudioOptions.NoAudio,
        output = OutputOptions(
            uri = SaveUri(uri, saveUri.type)
        )
    )
}