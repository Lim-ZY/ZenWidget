package com.example.zenwidget.data

import androidx.room.TypeConverter

enum class RepoType {
    QUOTES,
    ACTIONS
}

class RepoTypeConverters {
    @TypeConverter
    fun toRepoType(value: String): RepoType {
        return enumValueOf<RepoType>(value)
    }

    @TypeConverter
    fun fromRepoType(repoType: RepoType): String {
        return repoType.name
    }
}