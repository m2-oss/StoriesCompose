package ru.m2.squaremeter.stories.presentation.model

import ru.m2.squaremeter.stories.domain.entity.ShownStories

internal data class StoriesState(
    val duration: Int,
    val stories: List<UiStories>,
    val shownStories: Set<ShownStories>?
) {

    val currentStories get() = stories.first { it.current }
    val currentStoriesIndex get() = stories.indexOfFirst { it.current }
    private val slides get() = currentStories.slides
    val currentSlide get() = slides.first { it.current }
    val currentSlideIndex get() = slides.indexOfFirst { it.current }
    val slidesCount get() = slides.size

    fun shownStories(
        shownStories: Set<ShownStories>
    ): StoriesState =
        copy(
            stories = stories.map { story ->
                val shownStory = shownStories.find { it.storiesId == story.id }
                story.copy(
                    shown = shownStory?.shown ?: false,
                    slides = story.slides.mapIndexed { index, slide ->
                        // выбираем текущий слайд для просмотра
                        // если сторис не просмотрена и не просмотрен ни один слайд
                        // или сторис просмотрена - выбираем 1ый слайд
                        // если сторис не просмотрена, но просмотрены слайды -
                        // выбираем следующий непросмотренный слайд
                        slide.copy(
                            current = index == if (shownStory == null || shownStory.shown) {
                                0
                            } else {
                                shownStory.maxShownSlideIndex + 1
                            }
                        )
                    }
                )
            },
            shownStories = shownStories
        )

    fun slide(newSlideIndex: Int): StoriesState =
        progress(
            progressState = UiSlide.ProgressState.START,
            progress = 0f,
            newSlideIndex = newSlideIndex
        )
            .currentSlide(newSlideIndex)

    fun refreshSlide(): StoriesState =
        progress(progressState = UiSlide.ProgressState.START, progress = 0f)

    fun finish(): StoriesState =
        progress(progressState = UiSlide.ProgressState.COMPLETE, progress = 1f)

    fun stories(
        newStoriesIndex: Int,
        newSlideIndex: Int
    ): StoriesState =
        progress(
            progressState = UiSlide.ProgressState.START,
            progress = 0f,
            newStoriesIndex = newStoriesIndex,
            newSlideIndex = newSlideIndex
        )
            .currentStory(newStoriesIndex)

    fun pause(): StoriesState =
        progress(
            progressState = UiSlide.ProgressState.PAUSE, progress = currentSlide.progress
        )

    fun resume(progress: Float = currentSlide.progress): StoriesState =
        progress(progressState = UiSlide.ProgressState.RESUME, progress = progress)

    private fun currentSlide(newCurrentIndex: Int): StoriesState =
        copy(
            stories = stories.mapIndexed { storiesIndex, uiStories ->
                if (storiesIndex == currentStoriesIndex) {
                    uiStories.copy(
                        slides = uiStories.slides.mapIndexed { slideIndex, uiSlide ->
                            uiSlide.copy(current = slideIndex == newCurrentIndex)
                        }
                    )
                } else {
                    uiStories
                }
            }
        )

    private fun currentStory(newStoriesIndex: Int): StoriesState =
        copy(
            stories = stories.mapIndexed { index, uiStories ->
                uiStories.copy(current = index == newStoriesIndex)
            }
        )

    private fun progress(
        progressState: UiSlide.ProgressState,
        progress: Float,
        newStoriesIndex: Int = currentStoriesIndex,
        newSlideIndex: Int = currentSlideIndex
    ): StoriesState =
        copy(
            stories = stories.mapIndexed { storiesIndex, uiStories ->
                if (storiesIndex == newStoriesIndex) {
                    uiStories.copy(
                        slides = uiStories.slides.mapIndexed { slideIndex, uiSlide ->
                            when {
                                slideIndex < newSlideIndex -> {
                                    uiSlide.copy(
                                        progressState = UiSlide.ProgressState.COMPLETE,
                                        progress = 1f
                                    )
                                }

                                slideIndex == newSlideIndex -> {
                                    uiSlide.copy(
                                        progressState = progressState,
                                        progress = progress
                                    )
                                }

                                else -> {
                                    uiSlide.copy(
                                        progressState = UiSlide.ProgressState.START,
                                        progress = 0f
                                    )
                                }
                            }
                        }
                    )
                } else {
                    uiStories.copy(
                        slides = uiStories.slides.mapIndexed { slideIndex, uiSlide ->
                            val currentSlideIndex = uiStories.slides.indexOfFirst { it.current }
                            if (slideIndex < currentSlideIndex) {
                                uiSlide.copy(
                                    progressState = UiSlide.ProgressState.COMPLETE,
                                    progress = 1f
                                )
                            } else {
                                uiSlide.copy(
                                    progressState = UiSlide.ProgressState.START,
                                    progress = 0f
                                )
                            }
                        }
                    )
                }
            }
        )

    companion object {

        fun initial(
            durationInSec: Int,
            stories: List<UiStories>,
            storiesId: String
        ): StoriesState =
            StoriesState(
                duration = durationInSec * 1000,
                stories = stories.map { uiStories ->
                    uiStories.copy(
                        slides = uiStories.slides.mapIndexed { slideIndex, uiSlide ->
                            uiSlide.copy(current = slideIndex == 0)
                        },
                        current = uiStories.id == storiesId
                    )
                },
                shownStories = null
            )
    }
}
