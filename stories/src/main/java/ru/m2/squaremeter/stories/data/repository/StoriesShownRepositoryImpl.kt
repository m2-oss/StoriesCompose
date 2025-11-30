package ru.m2.squaremeter.stories.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.m2.squaremeter.stories.data.mapper.map
import ru.m2.squaremeter.stories.data.source.StoriesDatabase
import ru.m2.squaremeter.stories.domain.entity.ShownStories
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository

internal class StoriesShownRepositoryImpl(db: StoriesDatabase) : StoriesShownRepository {

    private val shownStoriesDao by lazy { db.shownStoriesDao() }

    override suspend fun set(shownStories: List<ShownStories>) {
        shownStoriesDao.set(shownStories.map { it.map() })
    }

    override fun observe(): Flow<List<ShownStories>> {
        return shownStoriesDao.observe().map { list ->
            list.map { it.map() }
        }
    }

    override fun get(): List<ShownStories> {
        return shownStoriesDao.get().map { it.map() }
    }

    override fun actualize(storiesIds: List<String>) {
        shownStoriesDao.actualize(storiesIds)
    }
}
