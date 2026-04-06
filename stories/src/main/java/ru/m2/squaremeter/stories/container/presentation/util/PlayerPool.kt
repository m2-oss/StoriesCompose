package ru.m2.squaremeter.stories.container.presentation.util

import ru.m2.squaremeter.stories.container.presentation.model.PlayerHolder

interface PlayerPool {

    fun get(page: Int): PlayerHolder

    fun releaseAll()
}