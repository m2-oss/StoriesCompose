package ru.m2.squaremeter.stories.container.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.m2.squaremeter.stories.container.presentation.model.UiSlide
import ru.m2.squaremeter.stories.container.presentation.model.UiSlidesData
import ru.m2.squaremeter.stories.container.presentation.model.UiStories
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesData
import ru.m2.squaremeter.stories.domain.entity.ShownStories
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository

private const val LOG_TAG = "stories_lib_StoriesViewModel"

internal class StoriesViewModel(
    private val exoPlayer: ExoPlayer,
    private val storiesShownRepository: StoriesShownRepository
) : ViewModel() {

    private var currentJob: Job? = null
    private val mutableStateFlow = MutableStateFlow(StoriesState(exoPlayer = exoPlayer))
    val stateFlow: StateFlow<StoriesState> = mutableStateFlow.asStateFlow()

    fun init(data: UiStoriesData) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e(LOG_TAG, "Failed loading shown stories", throwable)
            mutableStateFlow.value = stateFlow.value.ready(ReadyState.ERROR)
        }) {
            val shownStories = withContext(Dispatchers.IO) {
                storiesShownRepository.get()
            }
            var videoStoriesKey = ""
            var videoSlideIndex = 0
            var videoUrl = ""
            mutableStateFlow.value = StoriesState.initial(
                stories = data.stories.map { story ->
                    val uiSlides = story.value.mapIndexed { index, slide ->
                        if (slide is UiSlidesData.Video) {
                            videoStoriesKey = story.key
                            videoSlideIndex = index
                            videoUrl = slide.url
                        }
                        UiSlide(duration = slide.duration, video = slide is UiSlidesData.Video)
                    }
                    UiStories(
                        id = story.key,
                        slides = uiSlides,
                        video = story.value.any { it is UiSlidesData.Video }
                    )
                },
                storiesId = data.storiesId,
                shownStories = shownStories,
                exoPlayer = exoPlayer
            )
            exoPlayer.stop()
            if (videoUrl.isNotEmpty()) {
                prepareVideo(videoUrl, videoStoriesKey, videoSlideIndex)
            }
        }.also { job ->
            currentJob?.let {
                if (it.isActive) {
                    it.cancel()
                }
            }
            currentJob = job
        }
    }

    fun prepareVideo(videoUrl: String, videoStoriesKey: String, videoSlideIndex: Int) {
        exoPlayer.apply {
            addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackstate: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                setPaused()
                            }

                            Player.STATE_READY -> {
                                val duration = exoPlayer.duration
                                if (duration <= 0L) return
                                val targetStoriesIndex =
                                    stateFlow.value.stories.indexOfFirst { it.id == videoStoriesKey }
                                mutableStateFlow.value = stateFlow.value.duration(
                                    targetStoriesIndex = targetStoriesIndex,
                                    targetSlideIndex = videoSlideIndex,
                                    duration = duration
                                )
                                setResumed()
                            }

                            Player.STATE_ENDED,
                            Player.STATE_IDLE -> {
                            }
                        }
                    }
                }
            )
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    fun isVideoNow(): Boolean =
        stateFlow.value.currentStories.video && stateFlow.value.currentSlide.video

    fun resumeVideo() {
        if (isVideoNow()) {
            exoPlayer.play()
        }
    }

    fun pauseVideo() {
        exoPlayer.pause()
    }

    fun nextVideo() {
        exoPlayer.pause()
        exoPlayer.seekToPrevious()
    }

    fun prevVideo() {
        exoPlayer.pause()
        exoPlayer.seekToPrevious()
    }

    fun stopVideo() {
        exoPlayer.stop()
    }

    fun setFinish() {
        mutableStateFlow.value = stateFlow.value.finish()
        stopVideo()
    }

    fun setIdle() {
        mutableStateFlow.value = stateFlow.value.ready(ReadyState.IDLE)
        stopVideo()
    }

    fun setNextSlide() {
        with(stateFlow.value) {
            val nextSlideIndex = currentSlideIndex + 1
            if (slidesCount == nextSlideIndex) {
                val newStoriesIndex = currentStoriesIndex + 1
                if (newStoriesIndex == stories.size) {
                    setFinish()
                } else {
                    setNextStories()
                }
            } else {
                mutableStateFlow.value = stateFlow.value.slide(nextSlideIndex)
                nextVideo()
            }
        }
    }

    fun setPreviousSlide() {
        with(stateFlow.value) {
            if (currentSlideIndex == 0) {
                if (currentStoriesIndex == 0) {
                    mutableStateFlow.value = stateFlow.value.refreshSlide()
                    prevVideo()
                } else {
                    setPreviousStories()
                }
            } else {
                val previousSlideIndex = currentSlideIndex - 1
                mutableStateFlow.value = stateFlow.value.slide(previousSlideIndex)
                prevVideo()
            }
        }
    }

    private fun setNextStories() {
        with(stateFlow.value) {
            val newStoriesIndex = currentStoriesIndex + 1
            val slideIndex = stories[newStoriesIndex].slides.indexOfFirst { it.current }
            mutableStateFlow.value = stateFlow.value.stories(
                newStoriesIndex = newStoriesIndex,
                newSlideIndex = slideIndex
            )
            nextVideo()
        }
    }

    private fun setPreviousStories() {
        with(stateFlow.value) {
            val newStoriesIndex = currentStoriesIndex - 1
            val slideIndex = stories[newStoriesIndex].slides.indexOfFirst { it.current }
            mutableStateFlow.value = stateFlow.value.stories(
                newStoriesIndex = newStoriesIndex,
                newSlideIndex = slideIndex
            )
            prevVideo()
        }
    }

    fun setPaused() {
        mutableStateFlow.value = stateFlow.value.pause()
        pauseVideo()
    }

    fun setResumed() {
        with(stateFlow.value) {
            val validStates = listOf(
                UiSlide.ProgressState.START,
                UiSlide.ProgressState.PAUSE
            )
            if (currentSlide.progressState !in (validStates)) return
            if (isVideoNow() && exoPlayer.playbackState != Player.STATE_READY) return
            setStoriesShown(currentStoriesIndex)
            mutableStateFlow.value = stateFlow.value.resume()
            resumeVideo()
        }
    }

    private fun setStoriesShown(page: Int) {
        val stories = stateFlow.value.stories[page]
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e(LOG_TAG, "Failed setting shown stories", throwable)
        }) {
            with(stories) {
                val shownStories = withContext(Dispatchers.IO) {
                    storiesShownRepository.get()
                }
                val currentSlideIndex =
                    slides.indexOfFirst { it.current }
                val shownStory =
                    shownStories.find { it.storiesId == id }

                /**
                Choosing the max shown slide index is necessary for border visibility on preview and next story's playback:
                - if any slides of a story aren't shown ([ShownStories] == null) - the first slide will be chosen
                - if current slide index is greater than current stored value ([ShownStories.maxShownSlideIndex]) - current slide will be chosen
                - otherwise current stored value ([ShownStories.maxShownSlideIndex]) will be chosen as all the slides might be shown but user could return to previous ones
                 */
                val maxShownSlideIndex =
                    when {
                        shownStory == null -> 0
                        currentSlideIndex > shownStory.maxShownSlideIndex -> currentSlideIndex
                        else -> shownStory.maxShownSlideIndex
                    }

                withContext(Dispatchers.IO) {
                    storiesShownRepository.set(
                        listOf(
                            ShownStories(
                                storiesId = id,
                                maxShownSlideIndex = maxShownSlideIndex,
                                /**
                                A story considers as a shown if it has already been or the current shown slide is the last one
                                 */
                                shown =
                                    shownStory?.shown == true ||
                                            maxShownSlideIndex == slides.lastIndex
                            )
                        )
                    )
                }
            }
        }
    }

    fun setStories(page: Int) {
        with(stateFlow.value) {
            when {
                page > currentStoriesIndex -> {
                    setNextStories()
                }

                page < currentStoriesIndex -> {
                    setPreviousStories()
                }

                else -> {
                }
            }
        }
    }

    fun setProgress(progress: Float) {
        if (stateFlow.value.currentSlide.progressState != UiSlide.ProgressState.RESUME) return
        mutableStateFlow.value = stateFlow.value.resume(progress)
    }

    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
}
