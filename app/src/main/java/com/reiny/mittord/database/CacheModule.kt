package com.reiny.mittord.database

import android.content.Context
import androidx.room.Room
import com.reiny.mittord.R
import com.reiny.mittord.database.dao.SemanticObjectDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface CacheModule {

    fun dao(): SemanticObjectDao

    class Base @Inject constructor(@ApplicationContext applicationContext: Context) : CacheModule {

        private val database by lazy {
            Room.databaseBuilder(
                applicationContext,
                DictionaryDatabase::class.java,
                applicationContext.getString(R.string.db_name)
            ).build()
        }

        override fun dao() = database.dao()
    }
}