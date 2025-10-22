package ru.m2.squaremeter.stories.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.m2.squaremeter.stories.data.util.convertFromString
import ru.m2.squaremeter.stories.data.util.convertToString
import ru.m2.squaremeter.stories.domain.entity.ShownStories
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository
import java.io.Serializable

private const val STORIES_SHOWN_KEY = "stories_shown_key"

internal class StoriesShownRepositoryImpl(
    private val keyValueStorage: SharedPreferences
) : StoriesShownRepository {

    private val shownStoriesFlow = MutableStateFlow(getShownStories())

    private val sharedPreferencesEditor by lazy { keyValueStorage.edit() }

    override suspend fun set(shownStories: ShownStories) {
        val set = getShownStories().toMutableSet()
        set.removeIf { it.storiesId == shownStories.storiesId }
        set.add(shownStories)

        putSerializableSet(STORIES_SHOWN_KEY, set)

        shownStoriesFlow.emit(set)
    }

    override fun observe(): Flow<Set<ShownStories>> =
        shownStoriesFlow.asStateFlow()

    override fun get(): Set<ShownStories> =
        shownStoriesFlow.value

    override fun actualize(storiesIds: Set<String>) {
        val current = getShownStories()
        val actual = current.filter { storiesIds.contains(it.storiesId) }.toSet()
        if (actual.isEmpty()) return

        putSerializableSet(STORIES_SHOWN_KEY, actual)
    }

    private fun getShownStories(): Set<ShownStories> =
        runCatching {
            getSerializableSet(STORIES_SHOWN_KEY)
                .map { it as ShownStories }
                .toSet()
        }.getOrDefault(emptySet())

    private fun putSerializableSet(key: String, valuesSet: Set<Serializable>) {
        val strings = valuesSet.mapNotNull {
            convertToString(it) { it.printStackTrace() }
        }.toSet()
        putStringSet(key, strings)
    }

    private fun getSerializableSet(key: String): Set<Serializable> =
        getStringSet(key, emptySet()).mapNotNull {
            convertFromString(it) { it.printStackTrace() }?.let { it as? Serializable }
        }.toSet()

    private fun putStringSet(key: String, value: Set<String>) {
        sharedPreferencesEditor.putStringSet(key, value).commit()
    }

    private fun getStringSet(key: String, defValue: Set<String>): Set<String> =
        keyValueStorage.getStringSet(key, defValue)!!
}
