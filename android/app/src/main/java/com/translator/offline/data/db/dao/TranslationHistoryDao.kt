package com.translator.offline.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.translator.offline.data.db.entity.TranslationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationHistoryDao {
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllTranslations(): Flow<List<TranslationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationHistoryEntity)

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteTranslation(id: String)

    @Query("DELETE FROM translation_history")
    suspend fun clearAll()

    @Query("SELECT * FROM translation_history WHERE id = :id")
    suspend fun getTranslationById(id: String): TranslationHistoryEntity?
}
