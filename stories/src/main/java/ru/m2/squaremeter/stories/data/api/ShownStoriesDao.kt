package ru.m2.squaremeter.stories.data.api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.m2.squaremeter.stories.data.dto.ShownStoriesDto

@Dao
interface ShownStoriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(shownStories: List<ShownStoriesDto>)

    @Query("SELECT * FROM ShownStories")
    fun observe(): Flow<List<ShownStoriesDto>>

    @Query("SELECT * FROM ShownStories")
    fun get(): List<ShownStoriesDto>

    @Query("DELETE FROM ShownStories WHERE storiesId NOT IN (:ids)")
    fun actualize(ids: List<String>)
}