package ru.m2.squaremeter.stories.preview.presentation.model

/**
 * Base data of story's preview, required for display.
 *
 * @param id Unique identifier of each stories.
 * @param imageData Information about image. Could be external (url) or internal (file path) link.
 * @param title Name of the story.
 * @param shown Indicator whether the story was shown. Impacts on outer border visibility.
 */
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
