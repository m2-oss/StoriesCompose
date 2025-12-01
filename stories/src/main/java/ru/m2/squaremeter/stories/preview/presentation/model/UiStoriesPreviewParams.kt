package ru.m2.squaremeter.stories.preview.presentation.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ru.m2.squaremeter.stories.presentation.util.Colors

/**
 * A set of parameters for stories preview UI customization
 */
data class UiStoriesPreviewParams(
    /**
     * Paddings of the list relative to its parent
     */
    val listPaddings: PaddingValues = PaddingValues(
        start = 16.dp,
        top = 24.dp,
        end = 16.dp,
        bottom = 16.dp
    ),
    /**
     * Arrangement between elements of the list
     */
    val listSpacedByArrangement: Dp = 8.dp,
    /**
     * Overall size of the frame's content
     */
    val size: DpSize = DpSize(width = 104.dp, height = 144.dp),
    /**
     * Size of shown indicator
     */
    val borderSize: Dp = 104.dp,
    /**
     * Thickness of shown indicator
     */
    val borderWidth: Dp = 2.dp,
    /**
     * Color of shown indicator
     */
    val borderColor: Color = Colors.systemOnWhiteOrange,
    /**
     * Shape of shown indicator
     */
    val borderShape: Shape = RoundedCornerShape(16.dp),
    /**
     * Padding of the image relative to its parent
     */
    val imagePadding: Dp = 4.dp,
    /**
     * Shape of image
     */
    val imageShape: Shape = RoundedCornerShape(12.dp),
    /**
     * Size of image
     */
    val imageSize: Dp = 96.dp,
    /**
     * Spacer between image and text
     */
    val spacerSize: Dp = 8.dp,
    /**
     * Color of text
     */
    val textColor: Color = Colors.systemOnWhiteStrong,
    /**
     * Style of text
     */
    val textStyle: TextStyle = TextStyle.Default,
    /**
     * Alignment of text
     */
    val textAlign: TextAlign = TextAlign.Center
)
