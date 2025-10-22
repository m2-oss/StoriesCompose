package ru.m2.squaremeter.stories.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.m2.squaremeter.stories.StoriesShownRepositoryFactory

internal class StoriesViewModelFactory(
    private val context: Context,
    private val storiesId: String,
    private val stories: Map<String, Int>,
    private val durationInSec: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StoriesViewModel(
            stories,
            storiesId,
            durationInSec,
            StoriesShownRepositoryFactory.getInstance(context)
        ) as T
}
