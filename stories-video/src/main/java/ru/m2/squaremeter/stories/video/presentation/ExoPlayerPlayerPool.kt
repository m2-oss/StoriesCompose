package ru.m2.squaremeter.stories.video.presentation

import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.container.presentation.PlayerPool
import ru.m2.squaremeter.stories.container.presentation.VideoPlayer

class ExoPlayerPlayerPool(private val players: List<ExoPlayer>) : PlayerPool {

    override fun get(page: Int): VideoPlayer =
        PlayerHolder(players[page % players.size])


    override fun releaseAll() {
        players.forEach {
            it.stop()
            it.release()
        }
    }
}