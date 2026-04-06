package ru.m2.squaremeter.stories.container.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.m2.squaremeter.stories.container.presentation.ConnectivityObserver
import ru.m2.squaremeter.stories.container.presentation.StoryVideoManager
import ru.m2.squaremeter.stories.container.presentation.model.UiSlide
import ru.m2.squaremeter.stories.container.presentation.model.UiSlidesData
import ru.m2.squaremeter.stories.container.presentation.model.UiStories
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesData
import ru.m2.squaremeter.stories.domain.entity.ShownStories
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository

private const val LOG_TAG = "stories_lib_StoriesViewModel"

internal class StoriesViewModel(
    private val videoPlayerManager: StoryVideoManager?,
    private val connectivityObserver: ConnectivityObserver,
    private val storiesShownRepository: StoriesShownRepository
) : ViewModel() {

    private var initJob: Job = Job().apply { cancel() }
    private var networkJob: Job = Job().apply { cancel() }
    private val mutableStateFlow =
        MutableStateFlow(StoriesState(playerPool = videoPlayerManager?.getPlayerPool()))
    val stateFlow: StateFlow<StoriesState> = mutableStateFlow.asStateFlow()

    fun init(data: UiStoriesData) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e(LOG_TAG, "Failed loading shown stories", throwable)
            mutableStateFlow.value = stateFlow.value.ready(ReadyState.ERROR)
        }) {
            val shownStories = withContext(Dispatchers.IO) {
                storiesShownRepository.get()
            }
            videoPlayerManager?.init(data)
            mutableStateFlow.value = StoriesState.initial(
                stories = data.stories.map { story ->
                    val uiSlides = story.value.mapIndexed { index, slide ->
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
                playerPool = videoPlayerManager?.getPlayerPool()
            )
            videoPlayerManager?.stopVideo(stateFlow.value.currentStoriesIndex)
            videoPlayerManager?.seekToVideo(
                storiesIndex = stateFlow.value.currentStoriesIndex,
                slideIndex = stateFlow.value.currentSlideIndex,
                storiesId = data.storiesId
            )
            observeNetwork()
        }.also { job ->
            if (initJob.isActive) {
                initJob.cancel()
            }
            initJob = job
        }
    }

    private fun observeNetwork() {
        connectivityObserver.isConnected
            .flowOn(Dispatchers.IO)
            .onEach {
                if (it) {
                    restartVideo()
                }
            }
            .catch {
                Log.e(LOG_TAG, "Failed observing network state", it)
            }
            .launchIn(viewModelScope)
            .also { job ->
                if (networkJob.isActive) {
                    networkJob.cancel()
                }
                networkJob = job
            }
    }

    fun restartVideo() {
        if (!isVideoNow()) return
        videoPlayerManager?.restartVideo(stateFlow.value.currentStoriesIndex)
    }

    fun updateDuration(duration: Long) {
        val video = videoPlayerManager?.getVideoStoriesId(
            stateFlow.value.currentStoriesIndex,
            stateFlow.value.currentStories.id
        )
        if (video == null) {
            Log.e(LOG_TAG, "video not found for duration update")
            return
        }
        val targetStoriesIndex =
            stateFlow.value.stories.indexOfFirst { it.id == video.storiesId }
        mutableStateFlow.value = stateFlow.value.duration(
            targetStoriesIndex = targetStoriesIndex,
            targetSlideIndex = video.slideIndex,
            duration = duration
        )
    }

    fun isVideoNow(
        storiesIndex: Int = stateFlow.value.currentStoriesIndex,
        slideIndex: Int = stateFlow.value.currentSlideIndex
    ): Boolean =
        stateFlow.value.stories[storiesIndex].video &&
                stateFlow.value.stories[storiesIndex].slides[slideIndex].video

    fun refreshVideo() {
        videoPlayerManager?.refreshVideo(stateFlow.value.currentStoriesIndex)
    }

    fun resumeVideo() {
        if (!isVideoNow()) return
        videoPlayerManager?.resumeVideo(stateFlow.value.currentStoriesIndex)
    }

    fun pauseVideo() {
        if (!isVideoNow()) return
        videoPlayerManager?.pauseVideo(stateFlow.value.currentStoriesIndex)
    }

    fun seekToVideo(
        storiesIndex: Int = stateFlow.value.currentStoriesIndex,
        slideIndex: Int = stateFlow.value.currentSlideIndex
    ) {
        if (!isVideoNow(storiesIndex, slideIndex)) return
        val storiesId = stateFlow.value.stories[storiesIndex].id
        videoPlayerManager?.seekToVideo(storiesIndex, slideIndex, storiesId)
    }

    fun nextVideo(nextSlideIndex: Int) {
        seekToVideo(slideIndex = nextSlideIndex)
    }

    fun prevVideo(previousSlideIndex: Int) {
        seekToVideo(slideIndex = previousSlideIndex)
    }

    fun stopVideo() {
        if (!isVideoNow()) return
        videoPlayerManager?.stopVideo(stateFlow.value.currentStoriesIndex)
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
                nextVideo(nextSlideIndex)
            }
        }
    }

    fun setPreviousSlide() {
        with(stateFlow.value) {
            if (currentSlideIndex == 0) {
                if (currentStoriesIndex == 0) {
                    mutableStateFlow.value = stateFlow.value.refreshSlide()
                    refreshVideo()
                } else {
                    setPreviousStories()
                }
            } else {
                val previousSlideIndex = currentSlideIndex - 1
                mutableStateFlow.value = stateFlow.value.slide(previousSlideIndex)
                prevVideo(previousSlideIndex)
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
            seekToVideo(newStoriesIndex, slideIndex)
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
            seekToVideo(newStoriesIndex, slideIndex)
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
                    pauseVideo()
                    setNextStories()
                }

                page < currentStoriesIndex -> {
                    pauseVideo()
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
        videoPlayerManager?.releaseAll()
        super.onCleared()
    }
}
