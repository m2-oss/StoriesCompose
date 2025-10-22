package ru.m2.squaremeter.stories.presentation.model

internal data class UiSlide(
    val progressState: ProgressState = ProgressState.START,
    val current: Boolean = false,
    val progress: Float = 0f
) {

    internal enum class ProgressState {
        START, RESUME, PAUSE, COMPLETE
    }
}
