package ru.m2.squaremeter.stories.video.presentation.util

import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder
import ru.m2.squaremeter.stories.container.presentation.util.PlayerPool
import ru.m2.squaremeter.stories.video.presentation.model.ExoPlayerHolder

internal class ExoPlayerPool(private val players: List<ExoPlayer>) : PlayerPool {

    override fun get(page: Int): PlayerHolder =
        ExoPlayerHolder(players[page % players.size])


    override fun releaseAll() {
        players.forEach {
            it.stop()
            it.release()
        }
    }
}