package io.heartworks.schedulevideoplayer.models

import android.app.PendingIntent
import android.net.Uri
import android.os.CountDownTimer
import androidx.annotation.StringRes
import kotlinx.coroutines.Job
import java.util.Calendar

data class State(
    val isShowInterface: Boolean = true,
    @StringRes val playerState: Int? = null,
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val isScheduled: Boolean = false,
    val countDownText: String? = null,
    val uri: Uri? = null,
    val fileName: String = "",
    val time: Calendar? = null,
    val timeString: String? = null,
)
