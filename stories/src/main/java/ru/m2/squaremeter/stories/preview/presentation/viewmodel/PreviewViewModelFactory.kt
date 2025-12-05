package ru.m2.squaremeter.stories.preview.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.m2.squaremeter.stories.StoriesShownRepositoryFactory

internal class PreviewViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PreviewViewModel(
            StoriesShownRepositoryFactory.getInstance(context)
        ) as T
}