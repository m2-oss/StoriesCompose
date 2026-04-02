package ru.m2.squaremeter.stories.container.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder

interface StoryVideoProvider {

    @Composable
    fun UpdateDuration(player: PlayerHolder, onDurationUpdated: (Long) -> Unit)

    @Composable
    fun VideoProgress(playerHolder: PlayerHolder, onNext: () -> Unit, onProgress: (Float) -> Unit)

    fun createManager(context: Context): StoryVideoManager
}