package com.translator.offline.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.translator.offline.data.db.dao.TranslationHistoryDao
import com.translator.offline.data.db.entity.TranslationHistoryEntity

@Database(
    entities = [TranslationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TranslationDatabase : RoomDatabase() {
    abstract fun translationHistoryDao(): TranslationHistoryDao
}
