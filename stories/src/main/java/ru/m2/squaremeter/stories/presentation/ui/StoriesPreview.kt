package ru.m2.squaremeter.stories.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import ru.m2.squaremeter.stories.presentation.model.UiStoriesPreviewParams
import ru.m2.squaremeter.stories.presentation.model.UiStoriesPreview
import ru.m2.squaremeter.stories.presentation.util.Colors

@Composable
internal fun StoriesPreview(
    stories: UiStoriesPreview,
    onClick: (String) -> Unit,
    storiesPreviewParams: UiStoriesPreviewParams
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .size(storiesPreviewParams.size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(stories.id) }
            )
    ) {
        Box(
            modifier = Modifier
                .size(storiesPreviewParams.borderSize)
                .run {
                    if (stories.shown) {
                        this
                    } else {
                        border(
                            width = storiesPreviewParams.borderWidth,
                            color = storiesPreviewParams.borderColor,
                            shape = storiesPreviewParams.borderShape
                        )
                    }
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(stories.imageData)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .padding(storiesPreviewParams.imagePadding)
                    .size(storiesPreviewParams.imageSize)
                    .clip(shape = storiesPreviewParams.imageShape)
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(storiesPreviewParams.spacerSize))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stories.title,
            color = storiesPreviewParams.textColor,
            style = storiesPreviewParams.textStyle,
            textAlign = storiesPreviewParams.textAlign
        )
    }
}

@Preview(showBackground = true, backgroundColor = Colors.SYSTEM_WHITE_PREVIEW_BACKGROUND)
@Composable
private fun PreviewStoriesPreview() {
    StoriesPreview(
        stories = UiStoriesPreview(
            id = "id",
            imageData = "",
            title = "Text value 15 letter 2 lines",
            shown = false
        ),
        onClick = {},
        storiesPreviewParams = UiStoriesPreviewParams()
    )
}
