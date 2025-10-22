package ru.m2.squaremeter.stories.presentation.model

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

data class UiStoriesPreviewParams(
    val listPaddings: PaddingValues = PaddingValues(
        start = 14.dp,
        top = 14.dp,
        end = 14.dp,
        bottom = 16.dp
    ),
    val listSpacedByArrangement: Dp = 4.dp,
    val size: DpSize = DpSize(width = 104.dp, height = 144.dp),
    val borderSize: Dp = 104.dp,
    val borderWidth: Dp = 2.dp,
    val borderColor: Color = Colors.systemOnWhiteOrange,
    val borderShape: Shape = RoundedCornerShape(16.dp),
    val imagePadding: Dp = 4.dp,
    val imageShape: Shape = RoundedCornerShape(12.dp),
    val imageSize: Dp = 96.dp,
    val spacerSize: Dp = 8.dp,
    val textColor: Color = Colors.systemOnWhiteStrong,
    val textStyle: TextStyle = TextStyle.Default,
    val textAlign: TextAlign = TextAlign.Center
)
