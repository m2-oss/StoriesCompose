package ru.m2.squaremeter.stories.preview.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.m2.squaremeter.stories.StoriesShownRepositoryFactory
import ru.m2.squaremeter.stories.preview.presentation.model.UiStoriesPreview

internal class PreviewViewModelFactory(
    private val context: Context,
    private val previews: List<UiStoriesPreview>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PreviewViewModel(
            previews,
            StoriesShownRepositoryFactory.getInstance(context)
        ) as T
}