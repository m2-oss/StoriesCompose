package ru.m2.squaremeter.stories.container.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastAny
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder
import ru.m2.squaremeter.stories.container.presentation.model.StoriesType
import ru.m2.squaremeter.stories.container.presentation.model.UiSlide
import ru.m2.squaremeter.stories.container.presentation.model.UiStories
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesParams
import ru.m2.squaremeter.stories.container.presentation.viewmodel.StoriesState
import ru.m2.squaremeter.stories.presentation.util.Colors
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private const val RATIO_TO_DISMISS = 5.0f
private const val ROTATION_DEGREES = 45f
private const val TAP_MAX_THRESHOLD_MS = 250

@Composable
internal fun HorizontalPagerContainer(
    pagerState: PagerState,
    storiesState: StoriesState,
    storiesTypes: List<StoriesType>,
    tapInProgress: MutableState<Boolean>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinished: () -> Unit,
    onProgress: (Float) -> Unit,
    storiesParams: UiStoriesParams,
    content: @Composable BoxScope.(String, Int, Dp, PlayerHolder) -> Unit
) {
    val screenWidthPx = LocalWindowInfo.current.containerSize.width.toFloat()
    val screenHeightPx = LocalWindowInfo.current.containerSize.height.toFloat()
    val offsetYAnimatable = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var pagerScrollEnabled by remember { mutableStateOf(true) }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = pagerScrollEnabled,
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (storiesParams.transparentBackground) {
                    Color.Transparent
                } else {
                    Color.Black
                }
            )
            .offset { IntOffset(0, offsetYAnimatable.value.roundToInt()) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    tapInProgress.value = true
                    // Waiting for the moment to release the finger
                    val (isAtLeastOnePointerInputChangeConsumed, up) = waitForUp(down.id)
                    tapInProgress.value = false

                    // If no gestures were detected (i.e. "down" consumption
                    // and isAtLeastOnePointerInputChangeConsumed are false) and the duration
                    // between the lowering and raising of the finger is within
                    // the TAP_MAX_THRESHOLD_MS interval, then a tap on an empty area is detected
                    // and the slide must be switched
                    val nothingConsumed =
                        !down.isConsumed && !isAtLeastOnePointerInputChangeConsumed
                    val tapDetected = up?.let {
                        it.uptimeMillis - down.uptimeMillis <= TAP_MAX_THRESHOLD_MS
                    } ?: false
                    if (nothingConsumed && tapDetected) {
                        if (up.position.x < screenWidthPx / 2) {
                            onPrevious()
                        } else {
                            onNext()
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        pagerScrollEnabled = false
                    },
                    onDragEnd = {
                        if (offsetYAnimatable.value != 0f) {
                            val ratio = screenHeightPx / offsetYAnimatable.value
                            if (ratio <= RATIO_TO_DISMISS) {
                                animateDragging(screenHeightPx, scope, offsetYAnimatable)
                                onFinished()
                            } else {
                                animateDragging(0f, scope, offsetYAnimatable)
                                pagerScrollEnabled = true
                            }
                        }
                    },
                    onDragCancel = {},
                    onVerticalDrag = { _, dragAmount ->
                        val newOffset = offsetYAnimatable.value + dragAmount
                        if (newOffset >= 0) {
                            scope.launch {
                                offsetYAnimatable.snapTo(newOffset)
                            }
                        }
                    }
                )
            }
    ) { preloadedStoriesIndex ->
        HorizontalPagerContent(
            storiesTypes,
            pagerState,
            preloadedStoriesIndex,
            storiesState.exoPlayer,
            onNext,
            onProgress,
            storiesParams,
            content
        )
    }
}

/**
 * The [androidx.compose.foundation.gestures.waitForUpOrCancellation] function was taken as a basis
 * and modified to suit the needs of the library. It waits for an "up" event for the selected
 * pointerId. It then returns an "up" PointerInputChange to the caller and notifies them
 * whether any intermediate PointerInputChanges have been consumed. Such consumption indicates,
 * for example, that a manual Story swiping has occurred or a certain button is clicked.
 */
private suspend fun AwaitPointerEventScope.waitForUp(
    pointerId: PointerId
): Pair<Boolean, PointerInputChange?> {
    var isAtLeastOnePointerInputChangeConsumed = false
    var event: PointerEvent
    do {
        event = awaitPointerEvent()
        if (event.changes.fastAny { it.isConsumed }) {
            isAtLeastOnePointerInputChangeConsumed = true
        }
        val changedToUp = event.changes.fastAny { change ->
            change.id == pointerId && change.changedToUpIgnoreConsumed()
        }
    } while (!changedToUp)
    return isAtLeastOnePointerInputChangeConsumed to event.changes.firstOrNull()
}

private fun animateDragging(
    targetValue: Float,
    scope: CoroutineScope,
    animatable: Animatable<Float, AnimationVector1D>
) {
    scope.launch {
        animatable.animateTo(
            targetValue = targetValue,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
}

@Composable
private fun HorizontalPagerContent(
    storiesTypes: List<StoriesType>,
    pagerState: PagerState,
    preloadedStoriesIndex: Int,
    exoPlayer: ExoPlayer,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit,
    storiesParams: UiStoriesParams,
    content: @Composable BoxScope.(String, Int, Dp, PlayerHolder) -> Unit
) {
    /**
     * [preloadedStoriesIndex] is a [androidx.compose.foundation.pager.Pager]'s item to handle
     * because of pre-fetching and it might diverse from the one visible on the screen
     * @see <a href="https://issuetracker.google.com/issues/289088847">pre-fetching</a>
     */
    val storyType = storiesTypes[preloadedStoriesIndex]
    if (storyType is StoriesType.Content) {
        val preloadedStory = storyType.content
        val preloadedSlideIndex = preloadedStory.slides.indexOfFirst { it.current }

        if (storiesParams.transparentBackground) {
            var background by remember { mutableStateOf(Color.Black) }
            background = run {
                val pageOffset =
                    pagerState.getOffsetDistanceInPages(preloadedStoriesIndex)
                val offScreenRight = pageOffset > 0
                val stories = storiesTypes.mapNotNull { (it as? StoriesType.Content)?.content }
                val first = stories.indexOf(preloadedStory) == 0
                val last = stories.indexOf(preloadedStory) == stories.lastIndex
                if ((offScreenRight && first) || (!offScreenRight && last)) {
                    Color.Transparent
                } else {
                    Color.Black
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
            ) {
                ContentContainer(
                    preloadedStory,
                    preloadedSlideIndex,
                    pagerState,
                    preloadedStoriesIndex,
                    exoPlayer,
                    onNext,
                    onProgress,
                    storiesParams,
                    content
                )
            }
        } else {
            ContentContainer(
                preloadedStory,
                preloadedSlideIndex,
                pagerState,
                preloadedStoriesIndex,
                exoPlayer,
                onNext,
                onProgress,
                storiesParams,
                content
            )
        }
    }
}

@Composable
private fun ContentContainer(
    preloadedStory: UiStories,
    preloadedSlideIndex: Int,
    pagerState: PagerState,
    preloadedStoriesIndex: Int,
    exoPlayer: ExoPlayer,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit,
    storiesParams: UiStoriesParams,
    content: @Composable BoxScope.(String, Int, Dp, PlayerHolder) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .run {
                if (storiesParams.graphicsTransition) {
                    this.graphicsLayer {
                        val pageOffset =
                            pagerState.getOffsetDistanceInPages(preloadedStoriesIndex)
                        val offScreenRight = pageOffset > 0
                        val interpolated =
                            FastOutLinearInEasing.transform(pageOffset.absoluteValue)
                        rotationY =
                            interpolated *
                                    if (offScreenRight) ROTATION_DEGREES else -ROTATION_DEGREES

                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (offScreenRight) 0f else 1f,
                            pivotFractionY = .5f
                        )
                    }
                } else {
                    this
                }
            }
    ) {
        content(
            preloadedStory.id,
            preloadedSlideIndex,
            storiesParams.progressBarHeight,
            PlayerHolder(exoPlayer)
        )
        Stepper(
            modifier = if (storiesParams.fullScreen) {
                Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
            } else {
                Modifier
            },
            storiesIndex = preloadedStoriesIndex,
            slides = preloadedStory.slides,
            onNext = onNext,
            onProgress = onProgress,
            storiesParams = storiesParams,
            playerHolder = PlayerHolder(exoPlayer)
        )
    }
}

@Preview(showBackground = true, backgroundColor = Colors.SYSTEM_WHITE_PREVIEW_BACKGROUND)
@Composable
private fun HorizontalPagerContainerPreview() {
    HorizontalPagerContainer(
        pagerState = PagerState { 1 },
        storiesState = StoriesState.initial(
            stories = listOf(
                UiStories(
                    id = "",
                    slides = listOf(UiSlide())
                )
            ),
            storiesId = "",
            shownStories = emptyList(),
            exoPlayer = ExoPlayer.Builder(LocalContext.current).build()
        ),
        storiesTypes = listOf(
            StoriesType.Content(
                UiStories(
                    id = "",
                    slides = listOf(
                        UiSlide(
                            current = true
                        )
                    ),
                    current = true
                )
            )
        ),
        tapInProgress = remember { mutableStateOf(false) },
        onPrevious = {},
        onNext = {},
        onFinished = {},
        onProgress = {},
        storiesParams = UiStoriesParams(),
        content = { _, _, _, _ -> }
    )
}
