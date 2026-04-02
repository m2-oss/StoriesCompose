package ru.m2.squaremeter.stories.video.presentation

import androidx.media3.common.MediaItem
import ru.m2.squaremeter.stories.container.presentation.PlayerPool
import ru.m2.squaremeter.stories.container.presentation.StoryVideoManager
import ru.m2.squaremeter.stories.container.presentation.model.UiSlidesData
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesData
import ru.m2.squaremeter.stories.container.presentation.model.UiVideo

internal class VideoPlayerManager(private val playerPool: ExoPlayerPlayerPool) : StoryVideoManager { // todo создавать пулл внутри класса? убрать жесть из постоянных пробросов storiesIndex

    private val videos = mutableListOf<UiVideo>()

    override fun getPlayerPool(): PlayerPool =
        playerPool

    override fun init(data: UiStoriesData) {
        videos.clear()
        data.stories.map { story ->
            story.value.mapIndexed { index, slide ->
                if (slide is UiSlidesData.Video) {
                    videos.add(
                        UiVideo(storiesId = story.key, slideIndex = index, url = slide.url)
                    )
                }
            }
        }
    }

    override fun seekToVideo(storiesIndex: Int, slideIndex: Int, storiesId: String) {
        val currentVideos = videos.filter { it.storiesId == storiesId }
        val videoIndex = currentVideos.indexOfFirst { it.slideIndex == slideIndex }
        prepareVideos(currentVideos, videoIndex, storiesIndex)
    }

    private fun prepareVideos(videos: List<UiVideo>, videoSlideIndex: Int, storiesIndex: Int) {
        val player = (playerPool.get(storiesIndex) as PlayerHolder).player
        player.apply {
            setMediaItems(videos.map { MediaItem.fromUri(it.url) })
            seekTo(videoSlideIndex, 0L)
            prepare()
        }
    }

    override fun restartVideo(storiesIndex: Int) {
        val player = (playerPool.get(storiesIndex) as PlayerHolder).player
        if (player.playerError == null) return
        player.prepare()
    }
    
    override fun getVideoStoriesId(storiesIndex: Int, storiesId: String): UiVideo? {
        val player = (playerPool.get(storiesIndex) as PlayerHolder).player
        val mediaItemIndex = player.currentMediaItemIndex
        val video = videos.filter { it.storiesId == storiesId }.getOrNull(mediaItemIndex)
        return video
    }

    override fun refreshVideo(storiesIndex: Int) {
        val player = (playerPool.get(storiesIndex) as PlayerHolder).player
        player.pause()
        player.seekToPrevious()
    }

    override fun resumeVideo(storiesIndex: Int) {
        val player = (playerPool.get(storiesIndex) as PlayerHolder).player
        player.play()
    }

    override fun pauseVideo(storiesIndex: Int) {
        val player = (playerPool.get(storiesIndex) as PlayerHolder).player
        player.pause()
    }

    override fun nextVideo(storiesIndex: Int, slideIndex: Int, storiesId: String) {
        seekToVideo(storiesIndex, slideIndex, storiesId)
    }

    override fun prevVideo(storiesIndex: Int, slideIndex: Int, storiesId: String) {
        seekToVideo(storiesIndex, slideIndex, storiesId)
    }

    override fun stopVideo(storiesIndex: Int) {
        val player = (playerPool.get(storiesIndex) as PlayerHolder).player
        player.stop()
    }

    override fun releaseAll() {
        playerPool.releaseAll()
    }
}