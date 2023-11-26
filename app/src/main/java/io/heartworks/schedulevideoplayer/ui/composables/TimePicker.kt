package io.heartworks.schedulevideoplayer.ui.composables

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.heartworks.schedulevideoplayer.R
import java.util.Calendar

@Composable
fun TimePicker(
    timeString: String?, onTimeSelected: (selectedHour: Int, selectedMinute: Int) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val hour = calendar[Calendar.HOUR_OF_DAY]
    val minute = calendar[Calendar.MINUTE]
    val timePicker = TimePickerDialog(
        context, { _, selectedHour: Int, selectedMinute: Int ->
            onTimeSelected(selectedHour, selectedMinute)
        }, hour, minute, false
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { timePicker.show() }) {
            Text(text = stringResource(R.string.select_time))
        }
        Spacer(Modifier.width(8.dp))
        timeString?.let {
            Text(
                it, color = Color.White
            )
        }
    }
}