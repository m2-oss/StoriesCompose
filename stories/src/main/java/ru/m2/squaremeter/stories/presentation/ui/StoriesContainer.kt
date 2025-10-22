package ru.m2.squaremeter.stories.presentation.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.m2.squaremeter.stories.presentation.model.UiStoriesParams
import ru.m2.squaremeter.stories.presentation.model.StoriesState
import ru.m2.squaremeter.stories.presentation.model.StoriesType
import ru.m2.squaremeter.stories.presentation.model.UiSlide
import ru.m2.squaremeter.stories.presentation.model.UiStories
import ru.m2.squaremeter.stories.presentation.util.Colors
import ru.m2.squaremeter.stories.presentation.viewmodel.StoriesViewModel
import ru.m2.squaremeter.stories.presentation.viewmodel.StoriesViewModelFactory

@Composable
fun StoriesContainer(
    storiesId: String,
    stories: Map<String, Int>,
    durationInSec: Int,
    storiesParams: UiStoriesParams = UiStoriesParams(),
    onStoriesChanged: (String, Int) -> Unit = { _, _ -> },
    onFinished: () -> Unit = {},
    content: @Composable BoxScope.(Int, Int, Dp) -> Unit
) {
    MaterialTheme {
        val viewModel: StoriesViewModel = viewModel(
            factory = StoriesViewModelFactory(
                LocalContext.current,
                storiesId,
                stories,
                durationInSec
            )
        )
        val storiesState = viewModel.stateFlow.collectAsStateWithLifecycle().value

        StoriesContent(
            storiesState = storiesState,
            onFinished = onFinished,
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
    content: @Composable BoxScope.(Int, Int, Dp) -> Unit
) {
    if (storiesState.shownStories == null) return
    val storiesTypes = storiesState.stories.addFakeStories()
    val pagerState =
        rememberPagerState(
            pageCount = { storiesTypes.size },
            initialPage = storiesTypes.indexOf(
                StoriesType.Content(storiesState.currentStories)
            )
        )
    // аналог pagerState.isScrollInProgress, сделан по 3 причинам:
    // 1. isScrollInProgress периодически отрабатывает с задержкой
    // 2. лишние вызовы onPaused() и onResumed()
    // 2. синхронизация гонки состояний - финиша onPress у detectTapGestures
    // и изменения currentPage пейджера на Fake сторис, для успешного закрытия экрана
    val tapInProgress = remember { mutableStateOf(false) }

    // смена сторис, слайда при тапе, а также их запуск
    LaunchStoriesLaunchedEffect(
        storiesState,
        pagerState,
        storiesTypes,
        tapInProgress,
        onPaused,
        onResumed,
        onFinished
    )
    // оповещение о смене сторис
    StoriesChangedLaunchedEffect(storiesState, onStoriesChanged)
    // закрытие сторис при тапе на последнем элементе
    CloseOnLastSlideTapLaunchedEffect(storiesState, onFinished)
    // ставим на паузу, если открыт какой-нибудь диалог и фрагмен не в фокусе
    WindowFocusLaunchedEffect(onResumed = onResumed, onPaused = onPaused)
    // смена сторис при свайпе
    // здесь не указан tapInProgress в качестве ключа тк он уже есть в LaunchStoriesLaunchedEffect
    // и будет дублирование onFinished
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
        // если сторис Fake, то: если пальцы пользователя отжаты - закрываем экран.
        // также игнорируем смену сторис
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
    LaunchedEffect(pagerState) {
        snapshotFlow {
            pagerState.currentPage
        }.collect {
            // дублирование логики из-за гонки состояний onPress у detectTapGestures
            // и изменения pagerState.currentPage
            // если сторис Fake, то: если пальцы пользователя отжаты - закрываем экран.
            // также игнорируем смену сторис
            if (storiesTypes[it] is StoriesType.Fake) {
                if (!tapInProgress.value) {
                    onFinished()
                }
                return@collect
            }
            val contentStoriesIndex = storiesState.stories.indexOf(
                (storiesTypes[it] as StoriesType.Content).content
            )
            // вызывается для синхронизации стейта вм и pager'а
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
            storiesId = ""
        ).shownStories(emptySet()),
        onPaused = {},
        onResumed = {},
        onNext = {},
        onPrevious = {},
        onProgress = {},
        onFinished = {},
        onStoriesChanged = { _, _ -> },
        onStoriesSet = {},
        storiesParams = UiStoriesParams().copy(slideBackground = { _, _ -> Color.Black }),
        content = { _, _, _ -> }
    )
}
