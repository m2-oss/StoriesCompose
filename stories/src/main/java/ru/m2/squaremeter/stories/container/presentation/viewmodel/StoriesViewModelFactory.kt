package ru.m2.squaremeter.stories.container.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import ru.m2.squaremeter.stories.StoriesShownRepositoryFactory
import ru.m2.squaremeter.stories.container.presentation.ConnectivityObserver
import ru.m2.squaremeter.stories.container.presentation.util.PlayerPool

internal class StoriesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @UnstableApi
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StoriesViewModel(
            playerPool = PlayerPool(
                List(3) {
                    ExoPlayer.Builder(context)
                        .setPauseAtEndOfMediaItems(true)
                        .setDeviceVolumeControlEnabled(true)
                        .build()
                }
            ),
            storiesShownRepository = StoriesShownRepositoryFactory.getInstance(context),
            connectivityObserver = ConnectivityObserver(context)
        ) as T
}
