package ru.m2.squaremeter.stories.data.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ShownStories")
data class ShownStoriesDto(
    @PrimaryKey val storiesId: String,
    @ColumnInfo(name = "max_shown_slide_index") val maxShownSlideIndex: Int,
    @ColumnInfo(name = "shown") val shown: Boolean
)