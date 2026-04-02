package ru.m2.squaremeter.stories.container.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.m2.squaremeter.stories.StoriesShownRepositoryFactory
import ru.m2.squaremeter.stories.container.presentation.ConnectivityObserver
import ru.m2.squaremeter.stories.container.presentation.StoryVideoPlugin

internal class StoriesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = // todo подключить hilt?
        StoriesViewModel(
            videoPlayerManager = StoryVideoPlugin.provider?.createManager(context),
            storiesShownRepository = StoriesShownRepositoryFactory.getInstance(context),
            connectivityObserver = ConnectivityObserver(context)
        ) as T
}
