package ru.m2.squaremeter.stories.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import ru.m2.squaremeter.stories.presentation.model.UiStoriesParams
import ru.m2.squaremeter.stories.presentation.model.StoriesState
import ru.m2.squaremeter.stories.presentation.model.StoriesType
import ru.m2.squaremeter.stories.presentation.model.UiSlide
import ru.m2.squaremeter.stories.presentation.model.UiStories
import ru.m2.squaremeter.stories.presentation.util.Colors
import ru.m2.squaremeter.stories.presentation.util.detectTapGestures
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private const val RATIO_TO_DISMISS = 5.0f
private const val ROTATION_DEGREES = 45f

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
    content: @Composable BoxScope.(Int, Int, Dp) -> Unit
) {
    val screenWidthPx = LocalWindowInfo.current.containerSize.width.toFloat()
    val screenHeightPx = LocalWindowInfo.current.containerSize.height.toFloat()
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .run {
                if (storiesParams.transparentBackground) {
                    this
                } else {
                    this.background(Color.Black)
                }
            }
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        tapInProgress.value = true
                        // waiting for user's tap-up event and ignoring the result
                        tryAwaitRelease()
                        tapInProgress.value = false
                    },
                    onTap = { offset ->
                        if (offset.x < screenWidthPx / 2) {
                            onPrevious()
                        } else {
                            onNext()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        if (offsetY.value != 0f) {
                            val ratio = screenHeightPx / offsetY.value
                            if (ratio <= RATIO_TO_DISMISS) {
                                scope.launch {
                                    offsetY.animateTo(
                                        targetValue = screenHeightPx,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                                onFinished()
                            } else {
                                scope.launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onDragCancel = {},
                    onVerticalDrag = { _, dragAmount ->
                        val newOffset = offsetY.value + dragAmount
                        if (newOffset >= 0) {
                            scope.launch {
                                offsetY.snapTo(newOffset)
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
            storiesState.duration,
            onNext,
            onProgress,
            storiesParams,
            content
        )
    }
}

@Composable
private fun HorizontalPagerContent(
    storiesTypes: List<StoriesType>,
    pagerState: PagerState,
    preloadedStoriesIndex: Int,
    duration: Int,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit,
    storiesParams: UiStoriesParams,
    content: @Composable BoxScope.(Int, Int, Dp) -> Unit
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .run {
                    if (storiesParams.slideBackground == null) {
                        this
                    } else {
                        this.background(
                            storiesParams.slideBackground(
                                preloadedStoriesIndex - 1,
                                preloadedSlideIndex
                            )
                        )
                    }
                }
                .run {
                    if (storiesParams.fullScreen) {
                        this
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    } else {
                        this
                    }
                }
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
                preloadedStoriesIndex - 1,
                preloadedSlideIndex,
                storiesParams.progressBarHeight
            )
            Stepper(
                storiesIndex = preloadedStoriesIndex,
                slides = preloadedStory.slides,
                duration = duration,
                onNext = onNext,
                onProgress = onProgress,
                storiesParams = storiesParams
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = Colors.SYSTEM_WHITE_PREVIEW_BACKGROUND)
@Composable
private fun HorizontalPagerContainerPreview() {
    HorizontalPagerContainer(
        pagerState = PagerState { 1 },
        storiesState = StoriesState.initial(
            durationInSec = 10,
            stories = listOf(
                UiStories(
                    id = "",
                    slides = listOf(UiSlide())
                )
            ),
            storiesId = ""
        ).shownStories(emptySet()),
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
        content = { _, _, _ -> }
    )
}
