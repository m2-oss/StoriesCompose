package ru.m2.squaremeter.stories.preview.presentation.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.m2.squaremeter.stories.presentation.util.Colors
import ru.m2.squaremeter.stories.preview.presentation.model.UiStoriesPreviewData
import ru.m2.squaremeter.stories.preview.presentation.model.UiStoriesPreviewParams
import ru.m2.squaremeter.stories.preview.presentation.viewmodel.PreviewViewModel
import ru.m2.squaremeter.stories.preview.presentation.viewmodel.PreviewViewModelFactory

/**
 * A simple horizontal list that displays preview of stories.
 *
 * @param previews List of basic data required for display.
 * @param onClick Callback called in case of clicking on an item.
 * @param storiesPreviewParams Optional parameters for UI customization.
 */
@Composable
fun StoriesPreviewList(
    previews: List<UiStoriesPreviewData>,
    onClick: (String) -> Unit,
    storiesPreviewParams: UiStoriesPreviewParams = UiStoriesPreviewParams()
) {
    val viewModel: PreviewViewModel = viewModel(
        factory = PreviewViewModelFactory(context = LocalContext.current)
    )
    LaunchedEffect(previews) {
        viewModel.init(previews)
    }
    val previewState = viewModel.stateFlow.collectAsStateWithLifecycle().value
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(storiesPreviewParams.listPaddings),
        horizontalArrangement = Arrangement.spacedBy(storiesPreviewParams.listSpacedByArrangement)
    ) {
        previewState.previews.forEach { story ->
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
        previews = listOf(
            UiStoriesPreviewData(
                id = "id",
                imageData = "",
                title = "Title 1"
            ),
            UiStoriesPreviewData(
                id = "id",
                imageData = "",
                title = "Title 2"
            )
        ),
        onClick = {}
    )
}
