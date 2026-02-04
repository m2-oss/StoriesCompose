package ru.m2.squaremeter.stories.container.presentation.model

/**
 * Basic data required for stories playback.
 * @param storiesId id determining which story will be displayed
 * @param stories pairs of [storiesId] as a key and lists of slides containing duration as a value.
 * Eventually, size of keys equals to size of stories and values are size of slides in each of them.
 */
data class UiStoriesData(
    val storiesId: String,
    val stories: Map<String, List<UiSlidesData>>
)

sealed class UiSlidesData(open val duration: Long) {

    data class Image(override val duration: Long) : UiSlidesData(duration)

    data class Video(val url: String) : UiSlidesData(0L)
}