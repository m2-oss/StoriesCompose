package ru.m2.squaremeter.stories.video.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.media3.common.Player
import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder
import ru.m2.squaremeter.stories.video.presentation.model.ExoPlayerHolder

@Composable
internal fun UpdateDurationDisposableEffect(
    player: PlayerHolder,
    onDurationUpdated: (Long) -> Unit
) {
    val player = (player as ExoPlayerHolder).player

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val duration = player.duration
                    if (duration <= 0L) return
                    onDurationUpdated(duration)
                }
            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
}