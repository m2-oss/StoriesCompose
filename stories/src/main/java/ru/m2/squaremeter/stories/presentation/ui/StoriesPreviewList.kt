package ru.m2.squaremeter.stories.presentation.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.m2.squaremeter.stories.presentation.model.UiStoriesPreviewParams
import ru.m2.squaremeter.stories.presentation.model.UiStoriesPreview
import ru.m2.squaremeter.stories.presentation.util.Colors

@Composable
fun StoriesPreviewList(
    stories: List<UiStoriesPreview>,
    onClick: (String) -> Unit,
    storiesPreviewParams: UiStoriesPreviewParams = UiStoriesPreviewParams()
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(storiesPreviewParams.listPaddings),
        horizontalArrangement = Arrangement.spacedBy(storiesPreviewParams.listSpacedByArrangement)
    ) {
        stories.forEach { story ->
            StoriesPreview(
                story,
                onClick,
                storiesPreviewParams
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = Colors.SYSTEM_WHITE_PREVIEW_BACKGROUND)
@Composable
private fun PreviewStoriesPreviewList() {
    StoriesPreviewList(
        stories = listOf(
            UiStoriesPreview(
                id = "id",
                imageData = "",
                title = "Title 1",
                shown = false
            ),
            UiStoriesPreview(
                id = "id",
                imageData = "",
                title = "Title 2",
                shown = true
            )
        ),
        onClick = {}
    )
}
