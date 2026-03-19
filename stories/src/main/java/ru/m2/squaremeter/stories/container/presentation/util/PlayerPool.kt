package ru.m2.squaremeter.stories.container.presentation.util

import androidx.media3.exoplayer.ExoPlayer

class PlayerPool(private val players: List<ExoPlayer>) {

    fun get(page: Int): ExoPlayer =
        players[page % players.size]


    fun releaseAll() {
        players.forEach {
            it.stop()
            it.release()
        }
    }
}