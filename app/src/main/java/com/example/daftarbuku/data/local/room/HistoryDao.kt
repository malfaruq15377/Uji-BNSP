package com.example.daftarbuku.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.daftarbuku.data.local.model.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items ORDER BY id DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertHistory(historyItem: HistoryItem)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAllHistory(historyItems: List<HistoryItem>)
}