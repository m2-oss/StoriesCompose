package ru.m2.squaremeter.stories.container.presentation.model

internal data class UiSlide(
    val progressState: ProgressState = ProgressState.START,
    val current: Boolean = false,
    val progress: Float = 0f,
    val duration: Int = 0
) {

    internal enum class ProgressState {
        START, RESUME, PAUSE, COMPLETE
    }
}
