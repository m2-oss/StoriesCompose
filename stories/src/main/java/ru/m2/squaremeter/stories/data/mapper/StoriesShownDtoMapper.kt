package ru.m2.squaremeter.stories.data.mapper

import ru.m2.squaremeter.stories.data.dto.ShownStoriesDto
import ru.m2.squaremeter.stories.domain.entity.ShownStories

fun ShownStories.map(): ShownStoriesDto =
    ShownStoriesDto(
        storiesId,
        maxShownSlideIndex,
        shown
    )