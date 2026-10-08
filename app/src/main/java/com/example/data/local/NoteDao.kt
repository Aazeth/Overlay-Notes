package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<Note?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdSync(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE notes SET content = :content, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateContent(id: Long, content: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET widthDp = :widthDp, heightDp = :heightDp, posX = :posX, posY = :posY, opacity = :opacity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateGeometry(
        id: Long,
        widthDp: Int,
        heightDp: Int,
        posX: Int,
        posY: Int,
        opacity: Float,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE notes SET isLocked = :isLocked, isMinimized = :isMinimized WHERE id = :id")
    suspend fun updateStatus(id: Long, isLocked: Boolean, isMinimized: Boolean)
}
