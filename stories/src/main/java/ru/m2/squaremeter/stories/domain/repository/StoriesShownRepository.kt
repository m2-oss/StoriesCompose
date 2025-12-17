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
    fun set(shownStories: List<ShownStories>)

    /**
     * Observing (async operation) shown stories from memory.
     */
    fun observe(): Flow<List<ShownStories>>

    /**
     * Fetching (sync operation) shown stories from memory.
     */
    fun get(): List<ShownStories>

    /**
     * Updating info about shown stories by replacing old stories with new ones.
     * @param storiesIds set of ids to retain in memory.
     */
    fun actualize(storiesIds: List<String>)

    /**
     * Deletes all shown stories' data from memory
     */
    fun deleteAll()
}
