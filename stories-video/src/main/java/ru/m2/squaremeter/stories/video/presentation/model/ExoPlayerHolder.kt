package ru.m2.squaremeter.stories.video.presentation.model

import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder

data class ExoPlayerHolder(
    val player: ExoPlayer
) : PlayerHolder