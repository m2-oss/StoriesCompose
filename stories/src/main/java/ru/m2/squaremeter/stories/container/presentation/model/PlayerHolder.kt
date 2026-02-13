package ru.m2.squaremeter.stories.container.presentation.model

import androidx.compose.runtime.Stable
import androidx.media3.exoplayer.ExoPlayer

@Stable
data class PlayerHolder(
    val player: ExoPlayer
)