package io.heartworks.schedulevideoplayer.ui.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.heartworks.schedulevideoplayer.R

@Composable
fun VideoPicker(fileName: String, onPicked: (uri: Uri?) -> Unit) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            onPicked(it)
        })
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = {
            picker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }) {
            Text(stringResource(R.string.pick_video))
        }
        Spacer(Modifier.width(8.dp))
        Text(fileName, color = Color.White)
    }
}