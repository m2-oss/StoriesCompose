package ru.m2.squaremeter.stories.video

import android.content.Context
import androidx.startup.Initializer
import ru.m2.squaremeter.stories.container.presentation.StoryVideoPlugin
import ru.m2.squaremeter.stories.video.presentation.ExoPlayerVideoProvider

class VideoModuleInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        StoryVideoPlugin.provider = ExoPlayerVideoProvider()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}