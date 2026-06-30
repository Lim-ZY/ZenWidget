package com.example.zenwidget.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget_items")
data class RepoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val repoType: RepoType,
    val text: String,
    val caption: String
)