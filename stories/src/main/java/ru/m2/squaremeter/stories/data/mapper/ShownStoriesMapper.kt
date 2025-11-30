package ru.m2.squaremeter.stories.data.mapper

import ru.m2.squaremeter.stories.data.dto.ShownStoriesDto
import ru.m2.squaremeter.stories.domain.entity.ShownStories

fun ShownStoriesDto.map(): ShownStories =
    ShownStories(storiesId, maxShownSlideIndex, shown)