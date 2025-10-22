package ru.m2.squaremeter.stories.presentation.viewmodel

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
import ru.m2.squaremeter.stories.domain.entity.ShownStories
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository
import ru.m2.squaremeter.stories.presentation.model.StoriesState
import ru.m2.squaremeter.stories.presentation.model.UiSlide
import ru.m2.squaremeter.stories.presentation.model.UiStories

private const val LOG_TAG = "stories_lib_StoriesViewModel"

internal class StoriesViewModel(
    stories: Map<String, Int>,
    storiesId: String,
    durationInSec: Int,
    private val storiesShownRepository: StoriesShownRepository
) : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(
        StoriesState.initial(
            durationInSec = durationInSec,
            stories = stories.map { story ->
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
            storiesId = storiesId
        )
    )
    val stateFlow: StateFlow<StoriesState> = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e(LOG_TAG, "Failed loading shown stories", throwable)
            mutableStateFlow.value = stateFlow.value.shownStories(emptySet())
        }) {
            val shownStories = withContext(Dispatchers.IO) {
                storiesShownRepository.get()
            }

            mutableStateFlow.value = stateFlow.value.shownStories(shownStories)
        }
    }

    fun setFinish() {
        mutableStateFlow.value = stateFlow.value.finish()
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
                // выбираем последний просмотренный слайд - необходимо для
                // актуализации рамки на превью и при повторном просмотре
                // если сторис не просмотрен ни один слайд (shownStory == null) -
                // 1ый слайд
                // если сторис текущий слайд больше последнего просмотренного ранее -
                // текущий слайд
                // если сторис текущий слайд меньше последнего просмотренного ранее -
                // последний просмотренный, тк все слайды могут быть просмотрены,
                // но пользователь переключится на предыдущие и выйдет
                val maxShownSlideIndex =
                    when {
                        shownStory == null -> 0
                        currentSlideIndex > shownStory.maxShownSlideIndex -> currentSlideIndex
                        else -> shownStory.maxShownSlideIndex
                    }

                withContext(Dispatchers.IO) {
                    storiesShownRepository.set(
                        ShownStories(
                            storiesId = id,
                            maxShownSlideIndex = maxShownSlideIndex,
                            // сторис просмотрена, если она ранее уже была просмотрена
                            // или последний просмотренный слайд ==
                            // последнему слайду в сторис
                            shown =
                                shownStory?.shown == true ||
                                        maxShownSlideIndex == slides.lastIndex
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
