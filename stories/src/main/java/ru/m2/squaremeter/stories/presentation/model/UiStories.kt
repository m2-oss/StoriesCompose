package ru.m2.squaremeter.stories.presentation.model

internal data class UiStories(
    val id: String,
    val slides: List<UiSlide>,
    val current: Boolean = false,
    val shown: Boolean = false
) {

    companion object {

        val NULL_OBJECT = UiStories(
            id = "",
            slides = emptyList()
        )
    }
}

internal sealed class StoriesType {

    data class Content(val content: UiStories) : StoriesType()

    data object Fake : StoriesType()
}
