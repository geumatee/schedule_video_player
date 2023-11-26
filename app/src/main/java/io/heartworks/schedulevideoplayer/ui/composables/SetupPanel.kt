package io.heartworks.schedulevideoplayer.ui.composables

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.heartworks.schedulevideoplayer.R
import java.util.Calendar


@Composable
fun SetupPanel(
    modifier: Modifier,
    fileName: String,
    time: Calendar?,
    timeString: String?,
    isReady: Boolean,
    isScheduled: Boolean,
    @StringRes playerState: Int?,
    isPlaying: Boolean,
    onPicked: (uri: Uri?) -> Unit,
    onSetTime: (selectedHour: Int, selectedMinute: Int) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        if (isScheduled) {
            if (isPlaying) {
                Button(onClick = onStop) {
                    Text(text = stringResource(R.string.stop))
                }
            } else {
                Button(onClick = onCancel) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        } else {
            VideoPicker(fileName = fileName, onPicked = onPicked)
            TimePicker(timeString, onTimeSelected = onSetTime)
            playerState?.let {
                Text(stringResource(it), color = Color.White)
            }
            time?.let {
                if (playerState != null) {
                    Button(
                        onClick = {
                            onStart()
                        },
                        enabled = isReady
                    )
                    {
                        Text(stringResource(R.string.start))
                    }
                }
            }
        }
    }
}