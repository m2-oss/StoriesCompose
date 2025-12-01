package ru.m2.squaremeter.stories

import android.content.Context
import androidx.room.Room
import ru.m2.squaremeter.stories.data.repository.StoriesShownRepositoryImpl
import ru.m2.squaremeter.stories.data.source.StoriesDatabase
import ru.m2.squaremeter.stories.domain.repository.StoriesShownRepository

/**
 * Factory for [StoriesShownRepository] creation, entry point to work with stories cache
 */
class StoriesShownRepositoryFactory private constructor() {

    companion object {

        @Volatile
        private var instance: StoriesShownRepository? = null

        fun getInstance(context: Context): StoriesShownRepository {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = StoriesShownRepositoryImpl(
                            Room.databaseBuilder(
                                context,
                                StoriesDatabase::class.java,
                                "StoriesDatabase"
                            ).build()
                        )
                    }
                }
            }
            return instance!!
        }
    }
}
