package io.heartworks.schedulevideoplayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.heartworks.schedulevideoplayer.receiver.AlarmReceiver
import io.heartworks.schedulevideoplayer.ui.composables.ComposableLifecycle
import io.heartworks.schedulevideoplayer.ui.composables.Permission
import io.heartworks.schedulevideoplayer.ui.composables.PlayTriggerReceiver
import io.heartworks.schedulevideoplayer.ui.composables.SetupPanel
import io.heartworks.schedulevideoplayer.ui.composables.VideoPlayer
import io.heartworks.schedulevideoplayer.ui.theme.ScheduleVideoPlayerTheme
import io.heartworks.schedulevideoplayer.utils.getFileName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.abs


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScheduleVideoPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    ScheduleVideoPlayer(applicationContext, viewModel())
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.onApplyWindowInsets(windowInsets)
        }
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun ScheduleVideoPlayer(applicationContext: Context?, viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .build().apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = false
                addListener(getExoplayerListener(
                    onPlaybackStateChanged = { playbackState ->
                        viewModel.setPlayerState(playbackState)
                    },
                    onIsPlayingChanged = { isPlayingState ->
                        viewModel.setPlaying(isPlayingState)
                    }
                ))
            }
    }
    Box {
        VideoPlayer(exoPlayer, onVideoClickListener = {
            viewModel.toggleInterface()
        })
        SetupPanel(
            Modifier
                .align(Alignment.BottomStart)
                .alpha(if (uiState.isShowInterface) 1.0f else 0f),
            uiState.fileName,
            uiState.time,
            uiState.timeString,
            uiState.isReady,
            uiState.isScheduled,
            uiState.playerState,
            uiState.isPlaying,
            onPicked = { uri ->
                Log.d("flow", "picked: ${uri?.pathSegments?.last() ?: "null"}")
                viewModel.setUri(uri, uri?.getFileName(context) ?: "")
                uri?.let {
                    exoPlayer.apply {
                        stop()
                        playWhenReady = false
                        Log.d("flow", "setMediaItem")
                        setMediaItem(MediaItem.fromUri(it))
                        Log.d("flow", "prepare")
                        prepare()
                    }
                }
            },
            onStart = {
                val time = uiState.time ?: return@SetupPanel
                val delayTime = time.timeInMillis - Calendar.getInstance().timeInMillis
                viewModel.setScheduled(true)
                if (delayTime < 0) {
                    viewModel.hideInterface()
                    exoPlayer.seekTo(abs(delayTime) % exoPlayer.duration)
                    exoPlayer.play()
                } else {
                    alarmManager?.apply {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || canScheduleExactAlarms()) {
                            val intent = Intent(context, AlarmReceiver::class.java)
                            val pendingIntent = PendingIntent.getBroadcast(
                                applicationContext,
                                0, intent,
                                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            ).apply {
                                setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    time.timeInMillis,
                                    this
                                )
                            }
                            viewModel.setPendingIntent(pendingIntent)
                        } else {
                            viewModel.setJob(
                                CoroutineScope(Dispatchers.Default).launch {
                                    delay(delayTime)
                                    withContext(Dispatchers.Main) {
                                        exoPlayer.play()
                                        viewModel.hideInterface()
                                    }
                                }
                            )
                        }
                    }
                    viewModel.setJobCountDownTimer(
                        CoroutineScope(Dispatchers.Default).launch {
                            val delayTimeFraction = delayTime % 1000
                            delay(delayTimeFraction)
                            val countDownTime = delayTime - (delayTimeFraction)
                            withContext(Dispatchers.Main) {
                                viewModel.setCountDownTimer(
                                    object : CountDownTimer(countDownTime, 1000) {
                                        override fun onTick(tick: Long) {
                                            viewModel.setCountDownText(tick)
                                        }

                                        override fun onFinish() {
                                            Handler(Looper.getMainLooper()).post {
                                                viewModel.hideInterface()
                                                exoPlayer.play()
                                            }
                                        }
                                    }.start()
                                )
                            }
                        }
                    )
                }
            },
            onCancel = {
                viewModel.pendingIntent?.let { alarmManager?.cancel(it) }
                viewModel.clearPendingIntent()
                viewModel.clearJob()
                viewModel.clearCountDownTimer()
                viewModel.clearJobCountDownTimer()
                viewModel.clearCountDownText()
                viewModel.setScheduled(false)
            },
            onSetTime = { selectedHour, selectedMinute ->
                viewModel.setTime(selectedHour, selectedMinute)
            },
            onStop = {
                if (uiState.isPlaying) {
                    exoPlayer.stop()
                    viewModel.setScheduled(false)
                    viewModel.setUri(null, "")
                }
            })
        if (uiState.isScheduled && !uiState.isPlaying) {
            uiState.countDownText?.let {
                Text(
                    it,
                    color = Color.White,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(if (uiState.isShowInterface) 1.0f else 0f)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )
            }
        }
        ComposableLifecycle(
            onEvent = { _, event ->
                val lifecycleTag = "Lifecycle"
                when (event) {
                    Lifecycle.Event.ON_CREATE -> Log.d(lifecycleTag, "onCreate")
                    Lifecycle.Event.ON_START -> Log.d(lifecycleTag, "OnStart")
                    Lifecycle.Event.ON_RESUME -> Log.d(lifecycleTag, "OnResume")
                    Lifecycle.Event.ON_PAUSE -> Log.d(lifecycleTag, "OnPause")
                    Lifecycle.Event.ON_STOP -> {
                        Log.d(lifecycleTag, "OnStop")
                        exoPlayer.pause()
                    }
                    Lifecycle.Event.ON_DESTROY -> Log.d(lifecycleTag, "OnDestroy")
                    else -> {}
                }
            },
            onDispose = {
                exoPlayer.run {
                    playWhenReady = false
                    clearVideoSurface()
                    release()
                }
            }
        )
        Permission()
        PlayTriggerReceiver(onReceive = {
            exoPlayer.play()
            viewModel.hideInterface()
        })
    }
}

fun getExoplayerListener(
    onPlaybackStateChanged: (playbackState: Int) -> Unit,
    onIsPlayingChanged: (isPlayingState: Boolean) -> Unit
) =
    object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            Log.i(
                "ExoPlayer", "playbackState: " + when (playbackState) {
                    Player.STATE_BUFFERING -> "Buffering"
                    Player.STATE_ENDED -> "Ended"
                    Player.STATE_IDLE -> "Idle"
                    Player.STATE_READY -> "Ready"
                    else -> "null"
                }
            )
            onPlaybackStateChanged(playbackState)
        }

        override fun onIsPlayingChanged(isPlayingState: Boolean) {
            super.onIsPlayingChanged(isPlayingState)
            Log.i("ExoPlayer", "isPlaying: $isPlayingState")
            onIsPlayingChanged(isPlayingState)
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.e("player", "error", error)
        }
    }