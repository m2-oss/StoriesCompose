package ru.m2.squaremeter.stories.container.presentation.viewmodel

import ru.m2.squaremeter.stories.domain.entity.ShownStories
import ru.m2.squaremeter.stories.container.presentation.model.UiSlide
import ru.m2.squaremeter.stories.container.presentation.model.UiStories

internal data class StoriesState(
    val stories: List<UiStories> = emptyList(),
    val ready: ReadyState = ReadyState.IDLE
) {

    val currentStories get() = stories.first { it.current }
    val currentStoriesIndex get() = stories.indexOfFirst { it.current }
    private val slides get() = currentStories.slides
    val currentSlide get() = slides.first { it.current }
    val currentSlideIndex get() = slides.indexOfFirst { it.current }
    val slidesCount get() = slides.size

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

    fun ready(ready: ReadyState): StoriesState =
        copy(ready = ready)

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
            stories: List<UiStories>,
            storiesId: String,
            shownStories: List<ShownStories>
        ): StoriesState =
            StoriesState(
                stories = stories.map { story ->
                    val shownStory = shownStories.find { it.storiesId == story.id }
                    story.copy(
                        shown = shownStory?.shown ?: false,
                        slides = story.slides.mapIndexed { index, slide ->
                            /**
                            Choosing current slide for display
                            The first slide will be chosen if:
                            - Neither the story nor any slides are shown (for the first time)
                            - The story is shown
                            In case the story is shown partially - the next unshown slide will be chosen
                             */
                            slide.copy(
                                current = index == if (shownStory == null || shownStory.shown) {
                                    0
                                } else {
                                    shownStory.maxShownSlideIndex + 1
                                }
                            )
                        },
                        current = story.id == storiesId
                    )
                }.sortedBy { it.shown },
                ready = ReadyState.PLAY
            )
    }
}

internal enum class ReadyState {
    IDLE, PLAY, ERROR
}