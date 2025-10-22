package ru.m2.squaremeter.stories.domain.entity

import java.io.Serializable

data class ShownStories(
    val storiesId: String,
    val maxShownSlideIndex: Int,
    val shown: Boolean
) : Serializable {
    companion object {

        private const val serialVersionUID: Long = 4535840374138443524L
    }
}
