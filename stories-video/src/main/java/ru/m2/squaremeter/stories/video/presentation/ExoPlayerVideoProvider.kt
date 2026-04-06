package ru.m2.squaremeter.stories.video.presentation

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.container.presentation.StoryVideoManager
import ru.m2.squaremeter.stories.container.presentation.StoryVideoProvider
import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder
import ru.m2.squaremeter.stories.video.presentation.ui.UpdateDurationDisposableEffect
import ru.m2.squaremeter.stories.video.presentation.ui.VideoProgressEffects
import ru.m2.squaremeter.stories.video.presentation.util.ExoPlayerPool

internal class ExoPlayerVideoProvider : StoryVideoProvider {

    @Volatile
    private var manager: StoryVideoManager? = null

    @OptIn(UnstableApi::class)
    override fun createManager(context: Context): StoryVideoManager {
        return manager ?: synchronized(this) {
            manager ?: ExoPlayerVideoManager(
                playerPool = ExoPlayerPool(
                    List(3) {
                        ExoPlayer.Builder(context.applicationContext)
                            .setPauseAtEndOfMediaItems(true)
                            .setDeviceVolumeControlEnabled(true)
                            .build()
                    }
                )
            ).also { manager = it }
        }
    }


    @Composable
    override fun UpdateDuration(
        player: PlayerHolder,
        onDurationUpdated: (Long) -> Unit
    ) {
        UpdateDurationDisposableEffect(player = player, onDurationUpdated = onDurationUpdated)
    }

    @Composable
    override fun VideoProgress(
        playerHolder: PlayerHolder,
        onNext: () -> Unit,
        onProgress: (Float) -> Unit
    ) {
        VideoProgressEffects(playerHolder = playerHolder, onNext = onNext, onProgress = onProgress)
    }
}