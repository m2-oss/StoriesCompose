package ru.m2.squaremeter.stories.preview.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository
import ru.m2.squaremeter.stories.preview.presentation.model.UiStoriesPreview
import kotlin.collections.map

internal class PreviewViewModel(
    previews: List<UiStoriesPreview>,
    storiesShownRepository: StoriesShownRepository
) : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(PreviewState())
    val stateFlow: StateFlow<PreviewState> = mutableStateFlow.asStateFlow()

    init {
        storiesShownRepository.observe()
            .map { shownStories ->
                previews.map { story ->
                    story.copy(
                        shown = shownStories.any {
                            it.storiesId == story.id && it.shown
                        }
                    )
                }
                    .sortedBy { it.shown }
                    .toList()
            }
            .onEach {
                mutableStateFlow.value = stateFlow.value.previews(it)
            }
            .catch {
                mutableStateFlow.value = stateFlow.value.previews(emptyList())
            }
            .launchIn(viewModelScope)
    }
}
