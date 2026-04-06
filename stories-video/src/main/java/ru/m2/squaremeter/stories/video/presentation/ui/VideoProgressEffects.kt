package ru.m2.squaremeter.stories.video.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.media3.common.Player
import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder
import ru.m2.squaremeter.stories.video.presentation.model.ExoPlayerHolder

@Composable
internal fun VideoProgressEffects(
    playerHolder: PlayerHolder,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit,
) {
    val player = (playerHolder as ExoPlayerHolder).player
    var progress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player, isPlaying) {
        if (isPlaying) {
            while (true) {
                withFrameMillis {
                    val duration = player.duration.coerceAtLeast(1L)
                    val current = player.currentPosition.coerceAtLeast(0L)

                    progress = (current.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    if (progress == 1f) {
                        onNext()
                    } else {
                        onProgress(progress)
                    }
                }
            }
        }
    }
}