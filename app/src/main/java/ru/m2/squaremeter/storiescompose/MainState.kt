package ru.m2.squaremeter.storiescompose

import ru.m2.squaremeter.stories.presentation.model.UiStoriesPreview

data class MainState(
    val preview: List<UiStoriesPreview> = listOf()
) {

    fun preview(preview: List<UiStoriesPreview>): MainState = copy(preview = preview)
}