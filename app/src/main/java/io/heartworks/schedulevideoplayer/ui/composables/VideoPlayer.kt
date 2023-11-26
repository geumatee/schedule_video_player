package io.heartworks.schedulevideoplayer.ui.composables

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView


@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(exoPlayer: ExoPlayer, onVideoClickListener: () -> Unit) {
    val context = LocalContext.current
    AndroidView(factory = {
        PlayerView(context).apply {
            useController = false
            player = exoPlayer
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnClickListener {
                onVideoClickListener()
            }
        }
    })
}