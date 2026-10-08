package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val backgroundColor: Long = 0xFFE3F2FD, // Sky Blue default
    val textColor: Long = 0xFF0D2D44,
    val headerColor: Long = 0xFFBBDEFB,
    val fontFamily: String = "sans-serif", // sans-serif, serif, monospace, cursive
    val fontSize: Int = 15,
    val isBold: Boolean = false,
    val cornerRadius: Int = 12,
    val borderWidth: Int = 1,
    val borderColor: Long = 0x3364B5F6,
    val elevation: Int = 0,
    val opacity: Float = 1.0f,
    val widthDp: Int = 280,
    val heightDp: Int = 300,
    val posX: Int = 120,
    val posY: Int = 220,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val isMinimized: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
