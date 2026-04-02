package ru.m2.squaremeter.stories.video.presentation

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.container.presentation.StoryVideoManager
import ru.m2.squaremeter.stories.container.presentation.StoryVideoProvider

class ExoPlayerVideoProvider : StoryVideoProvider { // todo context сюда?

    @OptIn(UnstableApi::class)
    override fun createManager(context: Context): StoryVideoManager =
        VideoPlayerManager(
            playerPool = ExoPlayerPlayerPool(
                List(3) {
                    ExoPlayer.Builder(context)
                        .setPauseAtEndOfMediaItems(true)
                        .setDeviceVolumeControlEnabled(true)
                        .build()
                }
            )
        )
}