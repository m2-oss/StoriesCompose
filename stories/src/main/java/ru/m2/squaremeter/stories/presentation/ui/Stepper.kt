package ru.m2.squaremeter.stories.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.m2.squaremeter.stories.presentation.model.UiStoriesParams
import ru.m2.squaremeter.stories.presentation.model.UiSlide
import ru.m2.squaremeter.stories.presentation.model.UiStories

private const val INITIAL_ANIMATION_VALUE = 0f
private const val TARGET_ANIMATION_VALUE = 1f
internal val STEPPER_TRACK_COLOR = Color(0x52FFFFFF)

@Composable
internal fun Stepper(
    storiesIndex: Int,
    slides: List<UiSlide>,
    duration: Int,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit,
    storiesParams: UiStoriesParams
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(storiesParams.progressBarHeight)
            .padding(storiesParams.progressBarPaddings),
        horizontalArrangement = Arrangement.spacedBy(storiesParams.progressBarSpacedByArrangement)
    ) {
        slides.forEach {
            LinearProgressIndicator(
                progress = { it.progress },
                modifier = Modifier
                    .weight(1f)
                    .height(storiesParams.progressHeight)
                    .clip(storiesParams.progressShape),
                color = storiesParams.progressColor,
                trackColor = storiesParams.progressTrackColor,
                strokeCap = StrokeCap.Butt,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }

    Progress(
        storiesIndex = storiesIndex,
        slides = slides,
        duration = duration,
        onNext = onNext,
        onProgress = onProgress
    )
}

@Composable
private fun Progress(
    storiesIndex: Int,
    slides: List<UiSlide>,
    duration: Int,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit
) {
    val slideIndex = slides.indexOfFirst { it.current }
    val slide = slides[slideIndex]
    val progressAnimatable = remember(slideIndex, storiesIndex) {
        Animatable(INITIAL_ANIMATION_VALUE)
    }
    val progressState = slide.progressState

    LaunchedEffect(slideIndex, storiesIndex, progressState) {
        when (progressState) {
            UiSlide.ProgressState.START -> {
            }

            UiSlide.ProgressState.RESUME -> {
                progressAnimatable.snapTo(slide.progress)

                val durationMillis = (TARGET_ANIMATION_VALUE - slide.progress) * duration
                progressAnimatable.animateTo(
                    targetValue = TARGET_ANIMATION_VALUE,
                    animationSpec = tween(
                        durationMillis = durationMillis.toInt(),
                        easing = LinearEasing
                    )
                ) {
                    onProgress(value)
                }
                onNext()
            }

            UiSlide.ProgressState.PAUSE -> {
                progressAnimatable.stop()
            }

            UiSlide.ProgressState.COMPLETE -> {
                progressAnimatable.snapTo(TARGET_ANIMATION_VALUE)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStepper() {
    Stepper(
        storiesIndex = 0,
        slides = UiStories.NULL_OBJECT.slides,
        duration = 10000,
        onNext = {},
        onProgress = {},
        storiesParams = UiStoriesParams()
    )
}
