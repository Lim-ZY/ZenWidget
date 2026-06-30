package com.example.zenwidget.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [RepoItem::class], version = 1, exportSchema = false)
@TypeConverters(RepoTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun zenDao(): ZenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zen_database"
                ).addCallback(DatabaseCallback()).build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateDatabase(database.zenDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: ZenDao) {
                dao.insertItem(
                    RepoItem(
                        repoType = RepoType.QUOTES,
                        text = "The only way to do great work is to love what you do.",
                        caption = "Steve Jobs"
                    )
                )
                dao.insertItem(
                    RepoItem(
                        repoType = RepoType.QUOTES,
                        text = "真诚清净平等正觉慈悲，看破放下自在随缘念佛。",
                        caption = "净空法师"
                    )
                )
                dao.insertItem(
                    RepoItem(
                        repoType = RepoType.QUOTES,
                        text = "夫物速成则疾亡，晚就则善终。 朝华之草，夕而零落；松柏之茂，隆寒不衰。 是以大雅君子恶速成。",
                        caption = "群书治要"
                    )
                )
                dao.insertItem(
                    RepoItem(
                        repoType = RepoType.ACTIONS,
                        text = "Drink some water.",
                        caption = "Relax~"
                    )
                )
                dao.insertItem(
                    RepoItem(
                        repoType = RepoType.ACTIONS,
                        text = "Take a few slow, deep breaths. Notice your belly rising, and falling.",
                        caption = "Mindfulness~"
                    )
                )
            }
        }
    }
}