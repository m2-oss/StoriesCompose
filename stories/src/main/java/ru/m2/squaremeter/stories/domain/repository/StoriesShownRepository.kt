package ru.m2.squaremeter.stories.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.m2.squaremeter.stories.domain.entity.ShownStories

interface StoriesShownRepository {

    suspend fun set(shownStories: ShownStories)

    fun observe(): Flow<Set<ShownStories>>

    fun get(): Set<ShownStories>

    fun actualize(storiesIds: Set<String>)
}
