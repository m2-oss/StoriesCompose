package ru.m2.squaremeter.storiescompose

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

class MainViewModel(storiesShownRepository: StoriesShownRepository) : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(MainState())
    val stateFlow: StateFlow<MainState> = mutableStateFlow.asStateFlow()

    init {
        storiesShownRepository.observe()
            .map { shownStories ->
                STORIES_PREVIEW_LIST
                    .map { story ->
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
                mutableStateFlow.value = stateFlow.value.preview(it)
            }
            .catch {
                mutableStateFlow.value = stateFlow.value.preview(emptyList())
            }
            .launchIn(viewModelScope)
    }
}
