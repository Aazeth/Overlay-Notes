package com.example.data.model

data class DefaultPreset(
    val backgroundColor: Long = 0xFFE3F2FD, // Sky Blue
    val textColor: Long = 0xFF0D2D44,
    val headerColor: Long = 0xFFBBDEFB,
    val fontFamily: String = "sans-serif",
    val fontSize: Int = 15,
    val isBold: Boolean = false,
    val cornerRadius: Int = 12,
    val borderWidth: Int = 1,
    val borderColor: Long = 0x3364B5F6,
    val elevation: Int = 0,
    val opacity: Float = 1.0f,
    val widthDp: Int = 280,
    val heightDp: Int = 300
)
