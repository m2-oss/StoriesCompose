package ru.m2.squaremeter.stories

import android.content.Context
import ru.m2.squaremeter.stories.data.repository.StoriesShownRepositoryImpl
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository

/**
 * Factory for [StoriesShownRepository] creation, entry point to work with stories cache
 */
private const val PREFS_NAME = "ru.m2.squaremeter.stories.StoriesShownPrefs"

class StoriesShownRepositoryFactory private constructor() {

    companion object {

        @Volatile
        private var instance: StoriesShownRepository? = null

        fun getInstance(context: Context): StoriesShownRepository {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = StoriesShownRepositoryImpl(
                            context.getSharedPreferences(
                                PREFS_NAME,
                                Context.MODE_PRIVATE
                            )
                        )
                    }
                }
            }
            return instance!!
        }
    }
}
