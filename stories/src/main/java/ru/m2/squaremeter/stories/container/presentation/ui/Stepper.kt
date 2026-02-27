package ru.m2.squaremeter.stories.container.presentation.ui

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder
import ru.m2.squaremeter.stories.container.presentation.model.UiSlide
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesParams

private const val INITIAL_ANIMATION_VALUE = 0f
private const val TARGET_ANIMATION_VALUE = 1f
internal val STEPPER_TRACK_COLOR = Color(0x52FFFFFF)

@Composable
internal fun Stepper(
    storiesIndex: Int,
    slides: List<UiSlide>,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit,
    storiesParams: UiStoriesParams,
    playerHolder: PlayerHolder,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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

    val slideIndex = slides.indexOfFirst { it.current }
    val slide = slides[slideIndex]
    if (slide.video) {
        if (slide.duration == 0L) return
        VideoProgress(
            playerHolder = playerHolder,
            onNext = onNext,
            onProgress = onProgress
        )
    } else {
        Progress(
            storiesIndex = storiesIndex,
            slides = slides,
            onNext = onNext,
            onProgress = onProgress,
        )
    }
}

@Composable
private fun Progress(
    storiesIndex: Int,
    slides: List<UiSlide>,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit
) {
    val slideIndex = slides.indexOfFirst { it.current }
    val slide = slides[slideIndex]
    val progressAnimatable = remember(slideIndex, storiesIndex) {
        Animatable(INITIAL_ANIMATION_VALUE)
    }
    val progressState = slide.progressState

    if (slide.duration == 0L) return

    LaunchedEffect(slideIndex, storiesIndex, progressState) {
        when (progressState) {
            UiSlide.ProgressState.START -> {
            }

            UiSlide.ProgressState.RESUME -> {
                progressAnimatable.snapTo(slide.progress)

                val durationMillis = (TARGET_ANIMATION_VALUE - slide.progress) * slide.duration
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

@Composable
private fun VideoProgress(
    playerHolder: PlayerHolder,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit,
) {
    val player = playerHolder.player
    var progress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }


    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player, isPlaying) {
        if (isPlaying) {
            while (true) {
                withFrameMillis {
                    val duration = player.duration.coerceAtLeast(1L)
                    val current = player.currentPosition.coerceAtLeast(0L)

                    progress = (current.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    if (progress == 1f) {
                        onNext()
                    } else {
                        onProgress(progress)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStepper() {
    Stepper(
        storiesIndex = 0,
        slides = listOf(UiSlide(current = true, progress = 0.5f), UiSlide(), UiSlide()),
        onNext = {},
        onProgress = {},
        storiesParams = UiStoriesParams(),
        playerHolder = PlayerHolder(ExoPlayer.Builder(LocalContext.current).build())
    )
}
