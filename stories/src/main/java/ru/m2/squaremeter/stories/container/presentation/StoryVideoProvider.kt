package ru.m2.squaremeter.stories.container.presentation

import android.content.Context
import androidx.compose.runtime.Composable

interface StoryVideoProvider {

    /*@Composable
    fun VideoContent(url: String, isPlaying: Boolean)*/

    fun createManager(context: Context): StoryVideoManager
}