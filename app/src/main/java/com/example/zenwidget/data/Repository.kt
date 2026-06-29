package com.example.zenwidget.data

import androidx.glance.GlanceId
import com.example.zenwidget.layout.LongTextLayoutData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.zenwidget.layout.computeIfAbsent as computeIfAbsentExt
/**
 * An fake in-memory repository to provide data for displaying different demo samples in
 * [com.example.zenwidget.layout.LongTextLayout]
 */
enum class RepoType {
    QUOTES,
    ACTIONS
}

class Repository {
    private var activeRepo: RepoType = RepoType.QUOTES
    private var itemIndex: Int = 0
    private var itemsCount: Int = 0
    private val data = MutableStateFlow(quotesList[0])

    fun data(): Flow<LongTextLayoutData> = data

    /**
     * Mimics refresh by returning a different data item from the demo data list.
     *
     * This allows us to try the layout with various texts pertaining to a specific kind of data.
     */
    fun refresh() {
        itemIndex = (itemIndex + 1) % itemsCount

        this.load()
    }

    fun switchRepo() {
        activeRepo = when (activeRepo) {
            RepoType.QUOTES -> RepoType.ACTIONS
            RepoType.ACTIONS -> RepoType.QUOTES
        }

        itemIndex = 0
        this.load()
    }

    /** Loads the data and updates the flow */
    fun load(): LongTextLayoutData {
        val currentList = when (activeRepo) {
            RepoType.QUOTES -> quotesList
            RepoType.ACTIONS -> actionList
        }

        itemsCount = currentList.size
        data.value = currentList[itemIndex]

        return data.value
    }

    companion object {
        private val repositories = mutableMapOf<GlanceId, Repository>()

        /**
         * Returns the repository instance for the given widget represented by [glanceId].
         */
        fun getRepo(glanceId: GlanceId): Repository {
            return synchronized(repositories) {
                repositories.computeIfAbsentExt(glanceId) {
                    Repository()
                }!!
            }
        }

        /**
         * Cleans up local data associated with the provided [glanceId].
         */
        fun cleanUp(glanceId: GlanceId) {
            synchronized(repositories) {
                repositories.remove(glanceId)
            }
        }

        val quotesList = listOf(
            LongTextLayoutData(
                key = "item 0",
                text = "This is allows for a longer text string. Specifically because the focus in this, layout is on the primary text.",
                caption = "Caption",
            ),
            LongTextLayoutData(
                key = "item 1",
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                caption = "Ut mollis",
            ),
            LongTextLayoutData(
                key = "item 2",
                text = "Cursus mattis molestie a iaculis at erat pellentesque adipiscing commodo elit at imperdiet dui accumsan sit amet.",
                caption = "Ipsum faucibus",
            ),
            LongTextLayoutData(
                key = "item 3",
                text = "Tellus orci ac auctor augue mauris augue neque gravida in fermentum et sollicitudin ac orci",
                caption = "Amet cursus"
            ),
            LongTextLayoutData(
                key = "item 4",
                text = "Dolor sit amet consectetur adipiscing elit duis tristique sollicitudin nibh sit amet commodo nulla facilisi nullam.",
                caption = "Amet cursus"
            ),
        )

        val actionList = listOf(
            LongTextLayoutData(
                key = "item 0",
                text = "Observe your senses",
                caption = "Caption",
            ),
            LongTextLayoutData(
                key = "item 1",
                text = "Drink some water",
                caption = "Ut mollis",
            ),
            LongTextLayoutData(
                key = "item 2",
                text = "Clear your table",
                caption = "Ipsum faucibus",
            ),
            LongTextLayoutData(
                key = "item 3",
                text = "Breathe",
                caption = "Amet cursus"
            ),
            LongTextLayoutData(
                key = "item 4",
                text = "Rest your eyes",
                caption = "Amet cursus"
            ),
        )
    }
}
