package ru.m2.squaremeter.stories.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.m2.squaremeter.stories.data.api.ShownStoriesDao
import ru.m2.squaremeter.stories.data.dto.ShownStoriesDto

@Database(entities = [ShownStoriesDto::class], version = 1)
abstract class StoriesDatabase : RoomDatabase() {

    abstract fun shownStoriesDao(): ShownStoriesDao
}