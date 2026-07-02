package com.example.zenwidget.data

import android.content.Context
import androidx.glance.GlanceId
import com.example.zenwidget.layout.LongTextLayoutData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * An fake in-memory repository to provide data for displaying different demo samples in
 * [com.example.zenwidget.layout.LongTextLayout]
 */

class Repository(private val dao: ZenDao) {
    private var activeRepo: RepoType = RepoType.QUOTES
    private var itemIndex: Int = 0
    private var itemsCount: Int = 0

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val data = MutableStateFlow(
        LongTextLayoutData("loading", "Loading...", "")
    )

    fun data(): Flow<LongTextLayoutData> = data

    fun refresh() {
        coroutineScope.launch {
            itemsCount = dao.getCount(activeRepo)
            if (itemsCount > 0) {
                itemIndex = (itemIndex + 1) % itemsCount
            }
            loadFromDb()
        }
    }

    fun switchRepo() {
        coroutineScope.launch {
            activeRepo = if (activeRepo == RepoType.QUOTES) {
                RepoType.ACTIONS
            } else {
                RepoType.QUOTES
            }

            itemsCount = dao.getCount(activeRepo)
            if (activeRepo == RepoType.ACTIONS && itemsCount > 0) {
                itemIndex = (0 until itemsCount).random()
            } else {
                itemIndex = 0
            }

            loadFromDb()
        }
    }


    suspend fun loadFromDb(): LongTextLayoutData {
        itemsCount = dao.getCount(activeRepo)

        val itemToDisplay = if (itemsCount == 0) {
            LongTextLayoutData(
                key = "empty",
                text = "No items in this repository yet. Open the app to add some!",
                caption = "Empty"
            )
        } else {
            // Ensure the index doesn't go out of bounds if items were deleted
            itemIndex = itemIndex % itemsCount

            // Fetch the specific row from Room
            val entity = dao.getItemAt(activeRepo, itemIndex)

            if (entity != null) {
                LongTextLayoutData(entity.id.toString(), entity.text, entity.caption)
            } else {
                LongTextLayoutData("error", "Error loading item.", "Error")
            }
        }

        // Emit the new data to the widget UI
        data.value = itemToDisplay
        return itemToDisplay
    }

    companion object {
        private val repositories = mutableMapOf<GlanceId, Repository>()

        /**
         * Returns the repository instance for the given widget represented by [glanceId].
         */
        fun getRepo(glanceId: GlanceId, context: Context): Repository {
            return synchronized(repositories) {
                repositories.getOrPut(glanceId) {
                    val dao = AppDatabase.getDatabase(context).zenDao()
                    Repository(dao)
                }
            }
        }

        fun cleanUp(glanceId: GlanceId) {
            synchronized(repositories) {
                repositories.remove(glanceId)
            }
        }
    }
}
