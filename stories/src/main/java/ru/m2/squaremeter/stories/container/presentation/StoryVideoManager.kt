package ru.m2.squaremeter.stories.container.presentation

import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesData
import ru.m2.squaremeter.stories.container.presentation.model.UiVideo
import ru.m2.squaremeter.stories.container.presentation.util.PlayerPool

interface StoryVideoManager {

    fun getPlayerPool(): PlayerPool

    fun init(data: UiStoriesData)

    fun seekToVideo(storiesIndex: Int, slideIndex: Int, storiesId: String)

    fun prepareVideos(storiesIndex: Int, storiesId: String)

    fun restartVideo(storiesIndex: Int)

    fun getVideoStoriesId(storiesIndex: Int, storiesId: String): UiVideo?

    fun refreshVideo(storiesIndex: Int)

    fun resumeVideo(storiesIndex: Int)

    fun pauseVideo(storiesIndex: Int)

    fun stopVideo(storiesIndex: Int)

    fun releaseAll()
}