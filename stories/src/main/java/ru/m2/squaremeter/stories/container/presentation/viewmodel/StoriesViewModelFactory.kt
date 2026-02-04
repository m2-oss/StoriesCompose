package ru.m2.squaremeter.stories.container.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.StoriesShownRepositoryFactory

internal class StoriesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @UnstableApi
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StoriesViewModel(
            ExoPlayer.Builder(context).build(),
            StoriesShownRepositoryFactory.getInstance(context)
        ) as T
}
