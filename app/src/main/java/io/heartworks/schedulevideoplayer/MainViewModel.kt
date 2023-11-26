package io.heartworks.schedulevideoplayer

import android.app.PendingIntent
import android.net.Uri
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import io.heartworks.schedulevideoplayer.models.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(State())
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private var _job: Job? = null
    private var _jobCountDownTimer: Job? = null
    private var _countDownTimer: CountDownTimer? = null
    private var _pendingIntent: PendingIntent? = null
    val pendingIntent: PendingIntent?
        get() = _pendingIntent

    fun toggleInterface() {
        _uiState.update { currentState ->
            currentState.copy(isShowInterface = !currentState.isShowInterface)
        }
    }

    fun hideInterface() {
        if (_uiState.value.isShowInterface) {
            _uiState.update { currentState ->
                currentState.copy(isShowInterface = false)
            }
        }
    }

    fun setPlayerState(playbackState: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                playerState = when (playbackState) {
                    Player.STATE_BUFFERING -> R.string.buffering
                    Player.STATE_ENDED -> R.string.ended
                    Player.STATE_IDLE -> R.string.idle
                    Player.STATE_READY -> R.string.ready
                    else -> null
                }, isReady = playbackState == Player.STATE_READY
            )
        }
    }

    fun setPlaying(isPlaying: Boolean) {
        if (_uiState.value.isPlaying != isPlaying) {
            _uiState.update { currentState ->
                currentState.copy(isPlaying = isPlaying)
            }
        }
    }

    fun setScheduled(isScheduled: Boolean) {
        if (_uiState.value.isScheduled != isScheduled) {
            _uiState.update { currentState ->
                currentState.copy(isScheduled = isScheduled)
            }
        }
    }

    fun setJob(job: Job) {
        _job = job
    }

    fun clearJob() {
        _job?.cancel()
        _job = null
    }

    fun setJobCountDownTimer(jobCountDownTimer: Job) {
        _jobCountDownTimer = jobCountDownTimer
    }

    fun clearJobCountDownTimer() {
        _jobCountDownTimer?.cancel()
        _jobCountDownTimer = null
    }

    fun setCountDownTimer(countDownTimer: CountDownTimer) {
        _countDownTimer = countDownTimer
    }

    fun clearCountDownTimer() {
        _countDownTimer?.cancel()
        _countDownTimer = null
    }

    fun setCountDownText(tick: Long) {
        viewModelScope.launch {
            val countDownText = withContext(Dispatchers.IO) {
                val tickSecond = (tick + 1000) / 1000
                val minutes = tickSecond / 60
                val seconds = tickSecond % 60
                return@withContext String.format("%02d:%02d", minutes, seconds)
            }
            if (_uiState.value.countDownText != countDownText) {
                _uiState.update { currentState ->
                    currentState.copy(countDownText = countDownText)
                }
            }
        }
    }

    fun clearCountDownText() {
        if (_uiState.value.countDownText != null) {
            _uiState.update { currentState ->
                currentState.copy(countDownText = null)
            }
        }
    }

    fun setPendingIntent(pendingIntent: PendingIntent?) {
        _pendingIntent = pendingIntent
    }

    fun clearPendingIntent() {
        _pendingIntent?.cancel()
        _pendingIntent = null
    }

    fun setUri(uri: Uri?, fileName: String) {
        _uiState.update { currentState ->
            currentState.copy(uri = uri, fileName = fileName)
        }
    }

    fun setTime(selectedHour: Int, selectedMinute: Int) {
        val oldTime = _uiState.value.time
        if (oldTime == null ||
            oldTime.get(Calendar.HOUR) != selectedHour ||
            oldTime.get(Calendar.MINUTE) != selectedMinute
        ) {
            viewModelScope.launch {
                val calendar = withContext(Dispatchers.IO) {
                    val calendar = Calendar.getInstance()
                    calendar.set(
                        calendar[Calendar.YEAR],
                        calendar[Calendar.MONTH],
                        calendar[Calendar.DATE],
                        selectedHour,
                        selectedMinute,
                        0,
                    )
                    calendar.set(Calendar.MILLISECOND, 0)
                    return@withContext calendar
                }
                val timeString = withContext(Dispatchers.IO) {
                    return@withContext String.format(
                        "%02d:%02d", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)
                    )
                }
                _uiState.update { currentState ->
                    currentState.copy(time = calendar, timeString = timeString)
                }
            }
        }
    }
}