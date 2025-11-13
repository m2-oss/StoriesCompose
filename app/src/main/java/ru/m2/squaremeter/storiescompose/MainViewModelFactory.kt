package ru.m2.squaremeter.storiescompose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.m2.squaremeter.stories.StoriesShownRepositoryFactory

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainViewModel(StoriesShownRepositoryFactory.getInstance(context)) as T
}