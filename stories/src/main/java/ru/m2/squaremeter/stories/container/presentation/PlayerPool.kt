package ru.m2.squaremeter.stories.container.presentation

interface PlayerPool {

    fun get(page: Int): VideoPlayer

    fun releaseAll()
}
