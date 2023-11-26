package io.heartworks.schedulevideoplayer.ui.composables

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.heartworks.schedulevideoplayer.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val exactAlarmPermissionState =
            rememberPermissionState(Manifest.permission.SCHEDULE_EXACT_ALARM)
        if (!exactAlarmPermissionState.status.isGranted) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)) {
                Button(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = { exactAlarmPermissionState.launchPermissionRequest() }) {
                    Text(stringResource(R.string.request_permission))
                }
            }
        }
    }
}