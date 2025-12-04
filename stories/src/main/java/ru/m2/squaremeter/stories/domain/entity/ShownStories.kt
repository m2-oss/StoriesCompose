package ru.m2.squaremeter.stories.domain.entity

/**
 * Entity represents story's shown state info.
 * @param storiesId unique id of a story
 * @param maxShownSlideIndex index of a slide which meets the next criteria:
 * - it must be the last shown as the next one should be displayed next time
 * - it must be the max one as user can return to the previous ones so it mustn't be decreased
 * @sample ru.m2.squaremeter.stories.container.presentation.viewmodel.StoriesViewModel.setStoriesShown
 */
data class ShownStories(
    val storiesId: String,
    val maxShownSlideIndex: Int,
    val shown: Boolean
)
