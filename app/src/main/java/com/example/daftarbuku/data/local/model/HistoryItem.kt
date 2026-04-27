package com.example.daftarbuku.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val productPrice: Long,
    val purchaseDate: String,
    val productImageUrl: String
)