package ru.m2.squaremeter.storiescompose

import kotlinx.serialization.Serializable

sealed class Screen(val route: String) {

    @Serializable
    object StoriesPreview : Screen("preview")

    @Serializable
    object StoriesContent : Screen("content")
}
