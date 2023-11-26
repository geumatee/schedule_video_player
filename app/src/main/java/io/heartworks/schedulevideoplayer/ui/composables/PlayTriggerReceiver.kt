package io.heartworks.schedulevideoplayer.ui.composables

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun PlayTriggerReceiver(onReceive: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(context, "play") {
        val intentFilter = IntentFilter("play")

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                onReceive()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                intentFilter,
                ComponentActivity.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(receiver, intentFilter)
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}