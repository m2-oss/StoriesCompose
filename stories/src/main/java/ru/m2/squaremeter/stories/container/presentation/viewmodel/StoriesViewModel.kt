package ru.m2.squaremeter.stories.container.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.m2.squaremeter.stories.container.presentation.model.UiSlide
import ru.m2.squaremeter.stories.container.presentation.model.UiStories
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesData
import ru.m2.squaremeter.stories.domain.entity.ShownStories
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository

private const val LOG_TAG = "stories_lib_StoriesViewModel"

internal class StoriesViewModel(
    data: UiStoriesData,
    private val storiesShownRepository: StoriesShownRepository
) : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(StoriesState())
    val stateFlow: StateFlow<StoriesState> = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e(LOG_TAG, "Failed loading shown stories", throwable)
            mutableStateFlow.value = stateFlow.value.ready(ReadyState.ERROR)
        }) {
            val shownStories = withContext(Dispatchers.IO) {
                storiesShownRepository.get()
            }
            mutableStateFlow.value = StoriesState.initial(
                durationInSec = data.durationInSec,
                stories = data.stories.map { story ->
                    val uiSlides = mutableListOf<UiSlide>().apply {
                        repeat(story.value) {
                            add(UiSlide())
                        }
                    }
                    UiStories(
                        id = story.key,
                        slides = uiSlides
                    )
                },
                storiesId = data.storiesId,
                shownStories = shownStories
            )
        }
    }

    fun setFinish() {
        mutableStateFlow.value = stateFlow.value.finish()
    }

    fun setIdle() {
        mutableStateFlow.value = stateFlow.value.ready(ReadyState.IDLE)
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
            }
        }
    }

    fun setPreviousSlide() {
        with(stateFlow.value) {
            if (currentSlideIndex == 0) {
                if (currentStoriesIndex == 0) {
                    mutableStateFlow.value = stateFlow.value.refreshSlide()
                } else {
                    setPreviousStories()
                }
            } else {
                val previousSlideIndex = currentSlideIndex - 1
                mutableStateFlow.value = stateFlow.value.slide(previousSlideIndex)
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
        }
    }

    fun setPaused() {
        mutableStateFlow.value = stateFlow.value.pause()
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
}
