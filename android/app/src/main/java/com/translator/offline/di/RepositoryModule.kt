package com.translator.offline.di

import android.content.Context
import androidx.room.Room
import com.translator.offline.data.db.TranslationDatabase
import com.translator.offline.data.db.dao.TranslationHistoryDao
import com.translator.offline.data.ml.NLLBTranslator
import com.translator.offline.data.repository.TranslationRepositoryImpl
import com.translator.offline.domain.repository.TranslationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TranslationDatabase {
        return Room.databaseBuilder(
            context,
            TranslationDatabase::class.java,
            TranslationDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTranslationHistoryDao(database: TranslationDatabase): TranslationHistoryDao {
        return database.translationHistoryDao()
    }

    @Provides
    @Singleton
    fun provideNLLBTranslator(@ApplicationContext context: Context): NLLBTranslator {
        return NLLBTranslator(context)
    }

    @Provides
    @Singleton
    fun provideTranslationRepository(
        dao: TranslationHistoryDao,
        nllbTranslator: NLLBTranslator
    ): TranslationRepository {
        return TranslationRepositoryImpl(dao, nllbTranslator)
    }
}
