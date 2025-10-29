package ru.m2.squaremeter.stories.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.m2.squaremeter.stories.domain.entity.ShownStories

/**
 * Class for stories storage.
 */
interface StoriesShownRepository {

    /**
     * Putting shown stories into memory.
     * @param shownStories info about shown story
     */
    suspend fun set(shownStories: ShownStories)

    /**
     * Observing (async operation) shown stories from memory.
     */
    fun observe(): Flow<Set<ShownStories>>

    /**
     * Fetching (sync operation) shown stories from memory.
     */
    fun get(): Set<ShownStories>

    /**
     * Updating info about shown stories by replacing old stories with new ones.
     * @param storiesIds set of ids to retain in memory. If an element already exists in memory then nothing happens.
     */
    fun actualize(storiesIds: Set<String>)
}
