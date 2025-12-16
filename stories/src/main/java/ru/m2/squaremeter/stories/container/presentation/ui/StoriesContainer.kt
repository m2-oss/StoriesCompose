package ru.m2.squaremeter.stories.container.presentation.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.m2.squaremeter.stories.container.presentation.model.StoriesType
import ru.m2.squaremeter.stories.container.presentation.model.UiSlide
import ru.m2.squaremeter.stories.container.presentation.model.UiStories
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesData
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesParams
import ru.m2.squaremeter.stories.container.presentation.viewmodel.ReadyState
import ru.m2.squaremeter.stories.container.presentation.viewmodel.StoriesState
import ru.m2.squaremeter.stories.container.presentation.viewmodel.StoriesViewModel
import ru.m2.squaremeter.stories.container.presentation.viewmodel.StoriesViewModelFactory
import ru.m2.squaremeter.stories.presentation.util.Colors

/**
 * A container creating base functionality for stories such as:
 * taps and swipes, progress bar transition, storing/selecting slides to display logic.
 * Note that UI display should be on user side.
 * @param data Basic data required for stories playback
 * @param storiesParams UI customization
 * @param onStoriesChanged callback when every story changes.
 * Next story id and slide index will be sent.
 * @param onFinished callback when the last story ends.
 * @param content UI part of a slide of a story. The scope is to place components relative to the container.
 * First arguments, story id and slide current index, are to find current story,
 * and the third one, progress bar height, is to place your content under it if necessary
 */
@Composable
fun StoriesContainer(
    data: UiStoriesData,
    storiesParams: UiStoriesParams = UiStoriesParams(),
    onStoriesChanged: (String, Int) -> Unit = { _, _ -> },
    onFinished: () -> Unit = {},
    content: @Composable BoxScope.(String, Int, Dp) -> Unit
) {
    MaterialTheme {
        val viewModel: StoriesViewModel = viewModel(
            factory = StoriesViewModelFactory(LocalContext.current)
        )
        LaunchedEffect(data) {
            viewModel.init(data)
        }
        val storiesState = viewModel.stateFlow.collectAsStateWithLifecycle().value

        StoriesContent(
            storiesState = storiesState,
            onFinished = {
                viewModel.setIdle()
                onFinished()
            },
            onPaused = {
                viewModel.setPaused()
            },
            onResumed = {
                viewModel.setResumed()
            },
            onStoriesSet = { contentStoriesIndex ->
                viewModel.setStories(contentStoriesIndex)
            },
            onPrevious = {
                viewModel.setPreviousSlide()
            },
            onNext = {
                viewModel.setNextSlide()
            },
            onProgress = {
                viewModel.setProgress(it)
            },
            onStoriesChanged = onStoriesChanged,
            storiesParams = storiesParams,
            content = content
        )
    }
}

@Composable
private fun StoriesContent(
    storiesState: StoriesState,
    onStoriesChanged: (String, Int) -> Unit,
    onFinished: () -> Unit,
    onPaused: () -> Unit,
    onResumed: () -> Unit,
    onStoriesSet: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onProgress: (Float) -> Unit,
    storiesParams: UiStoriesParams,
    content: @Composable BoxScope.(String, Int, Dp) -> Unit
) {
    when (storiesState.ready) {
        ReadyState.IDLE -> return
        ReadyState.ERROR -> {
            onFinished()
            return
        }
        ReadyState.PLAY -> {}
    }
    val storiesTypes = storiesState.stories.addFakeStories()
    val pagerState =
        rememberPagerState(
            pageCount = { storiesTypes.size },
            initialPage = storiesTypes.indexOf(
                StoriesType.Content(storiesState.currentStories)
            )
        )
    /**
     * similar to [PagerState.isScrollInProgress], there are 3 reasons for it:
     * - [PagerState.isScrollInProgress] occasionally delays
     * - excess [onPaused] and [onResumed] calls
     * - resolves race condition between
     * [ru.m2.squaremeter.stories.container.presentation.util.detectTapGestures] onPress
     * and change [PagerState.currentPage] to [StoriesType.Fake]
     * and therefore successfully finishes stories
      */
    val tapInProgress = remember { mutableStateOf(false) }

    // change and launch stories or slide with tap
    LaunchStoriesLaunchedEffect(
        storiesState,
        pagerState,
        storiesTypes,
        tapInProgress,
        onPaused,
        onResumed,
        onFinished
    )
    // change stories observer
    StoriesChangedLaunchedEffect(storiesState, onStoriesChanged)
    // close stories on last slide's tap
    CloseOnLastSlideTapLaunchedEffect(storiesState, onFinished)
    // pause a story if the container fragment isn't focused (e.g. a dialog)
    WindowFocusLaunchedEffect(onResumed = onResumed, onPaused = onPaused)
    // change stories during swipe
    SwipeStoriesLaunchedEffect(
        storiesState,
        pagerState,
        storiesTypes,
        tapInProgress,
        onFinished,
        onStoriesSet
    )

    HorizontalPagerContainer(
        pagerState,
        storiesState,
        storiesTypes,
        tapInProgress,
        onPrevious,
        onNext,
        onFinished,
        onProgress,
        storiesParams,
        content
    )
}

private fun List<UiStories>.addFakeStories(): List<StoriesType> {
    val list = this.map {
        StoriesType.Content(it)
    } as List<StoriesType>
    return list.toMutableList().apply {
        add(0, StoriesType.Fake)
        add(StoriesType.Fake)
    }
}

@Composable
private fun WindowFocusLaunchedEffect(onResumed: () -> Unit, onPaused: () -> Unit) {
    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) {
                onResumed()
            } else {
                onPaused()
            }
        }
    }
}

@Composable
private fun StoriesChangedLaunchedEffect(
    storiesState: StoriesState,
    onStoriesChanged: (String, Int) -> Unit
) {
    LaunchedEffect(
        storiesState.currentStoriesIndex,
        storiesState.currentSlideIndex
    ) {
        onStoriesChanged(storiesState.currentStories.id, storiesState.currentSlideIndex)
    }
}

@Composable
private fun LaunchStoriesLaunchedEffect(
    storiesState: StoriesState,
    pagerState: PagerState,
    storiesTypes: List<StoriesType>,
    tapInProgress: MutableState<Boolean>,
    onPaused: () -> Unit,
    onResumed: () -> Unit,
    onFinished: () -> Unit
) {
    LaunchedEffect(
        storiesState.currentStoriesIndex,
        storiesState.currentSlideIndex,
        tapInProgress.value
    ) {
        /**
         * If a story is [StoriesType.Fake] then ignores stories change.
         * Also, if all fingers left the display (![tapInProgress]) then finish stories
         */
        if (storiesTypes[pagerState.currentPage] is StoriesType.Fake) {
            if (!tapInProgress.value) {
                onFinished()
            }
            return@LaunchedEffect
        }
        val index = storiesTypes.indexOf(StoriesType.Content(storiesState.currentStories))
        if (index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }

        if (tapInProgress.value) {
            onPaused()
        } else {
            onResumed()
        }
    }
}

@Composable
private fun SwipeStoriesLaunchedEffect(
    storiesState: StoriesState,
    pagerState: PagerState,
    storiesTypes: List<StoriesType>,
    tapInProgress: MutableState<Boolean>,
    onFinished: () -> Unit,
    onStoriesSet: (Int) -> Unit
) {
    /**
     * tapInProgress isn't a key here to not duplicate onFinished call
     * @see [LaunchStoriesLaunchedEffect]
     */
    LaunchedEffect(pagerState) {
        snapshotFlow {
            pagerState.currentPage
        }.collect {
            /**
             * logic duplication because of race condition between
             * onPress from [ru.m2.squaremeter.stories.container.presentation.util.detectTapGestures]
             * and [PagerState.currentPage].
             * If a story is [StoriesType.Fake] then ignores stories change.
             * Also, if all fingers left the display (![tapInProgress]) then finish stories
              */
            if (storiesTypes[it] is StoriesType.Fake) {
                if (!tapInProgress.value) {
                    onFinished()
                }
                return@collect
            }
            val contentStoriesIndex = storiesState.stories.indexOf(
                (storiesTypes[it] as StoriesType.Content).content
            )
            /**
             * sync between [StoriesState] and [PagerState]
             */
            onStoriesSet(contentStoriesIndex)
        }
    }
}

@Composable
private fun CloseOnLastSlideTapLaunchedEffect(
    storiesState: StoriesState,
    onFinished: () -> Unit
) {
    LaunchedEffect(
        storiesState.currentSlide.progressState
    ) {
        with(storiesState) {
            if (currentSlide.progressState == UiSlide.ProgressState.COMPLETE &&
                currentStoriesIndex == this.stories.lastIndex &&
                currentSlideIndex == currentStories.slides.lastIndex
            ) {
                onFinished()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = Colors.SYSTEM_WHITE_PREVIEW_BACKGROUND)
@Composable
private fun PreviewStoriesContent() {
    StoriesContent(
        storiesState = StoriesState.initial(
            durationInSec = 10,
            stories = listOf(
                UiStories(
                    id = "",
                    slides = listOf(UiSlide())
                )
            ),
            storiesId = "",
            shownStories = emptyList()
        ),
        onPaused = {},
        onResumed = {},
        onNext = {},
        onPrevious = {},
        onProgress = {},
        onFinished = {},
        onStoriesChanged = { _, _ -> },
        onStoriesSet = {},
        storiesParams = UiStoriesParams(),
        content = { _, _, _ -> }
    )
}
