package com.example.zenwidget.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ZenDao {
    // Returns an observable stream of items for a specific repository
    @Query("SELECT * FROM widget_items WHERE repoType = :repoType")
    fun getItemsForRepo(repoType: Int): Flow<List<RepoItem>>

    @Insert
    suspend fun insertItem(item: RepoItem)

    // Optional: for cycling through items in the widget
    @Query("SELECT * FROM widget_items WHERE repoType = :repoType LIMIT 1 OFFSET :index")
    suspend fun getItemAt(repoType: Int, index: Int): RepoItem?

    @Query("SELECT COUNT(*) FROM widget_items WHERE repoType = :repoType")
    suspend fun getCount(repoType: Int): Int
}