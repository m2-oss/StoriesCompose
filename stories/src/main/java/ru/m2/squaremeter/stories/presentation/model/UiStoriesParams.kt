package ru.m2.squaremeter.stories.presentation.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.m2.squaremeter.stories.presentation.ui.STEPPER_TRACK_COLOR
import ru.m2.squaremeter.stories.presentation.util.Colors

data class UiStoriesParams(
    val fullScreen: Boolean = true,
    val graphicsTransition: Boolean = true,
    val transparentBackground: Boolean = true,
    val paddings: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    val spacedByArrangement: Dp = 4.dp,
    val progressBarHeight: Dp = 32.dp,
    val progressHeight: Dp = 4.dp,
    val progressShape: Shape = RoundedCornerShape(8.dp),
    val progressColor: Color = Colors.systemWhite,
    val progressTrackColor: Color = STEPPER_TRACK_COLOR,
    val slideBackground: ((Int, Int) -> Color)? = null
)
