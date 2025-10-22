package ru.m2.squaremeter.stories.presentation.model

data class UiStoriesPreview(
    val id: String,
    val imageData: Any,
    val title: String,
    val shown: Boolean
) {

    companion object {

        val NULL_OBJECT = UiStoriesPreview(
            id = "",
            imageData = "",
            title = "",
            shown = false
        )
    }
}
