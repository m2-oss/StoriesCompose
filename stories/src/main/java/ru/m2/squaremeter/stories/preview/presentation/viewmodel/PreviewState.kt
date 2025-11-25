package ru.m2.squaremeter.stories.preview.presentation.viewmodel

import ru.m2.squaremeter.stories.preview.presentation.model.UiStoriesPreview

internal data class PreviewState(
    val previews: List<UiStoriesPreview> = listOf()
) {

    fun previews(preview: List<UiStoriesPreview>): PreviewState = copy(previews = preview)
}