package ru.m2.squaremeter.stories.video.presentation

import androidx.compose.runtime.Stable
import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.container.presentation.VideoPlayer

@Stable
data class PlayerHolder( // todo delete?
    val player: ExoPlayer
) : VideoPlayer