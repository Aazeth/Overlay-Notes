package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)

    suspend fun getNoteByIdSync(id: Long): Note? = noteDao.getNoteByIdSync(id)

    suspend fun insert(note: Note): Long = noteDao.insert(note)

    suspend fun update(note: Note) = noteDao.update(note)

    suspend fun delete(note: Note) = noteDao.delete(note)

    suspend fun deleteById(id: Long) = noteDao.deleteById(id)

    suspend fun updateContent(id: Long, content: String) =
        noteDao.updateContent(id, content)

    suspend fun updateGeometry(
        id: Long,
        widthDp: Int,
        heightDp: Int,
        posX: Int,
        posY: Int,
        opacity: Float
    ) = noteDao.updateGeometry(id, widthDp, heightDp, posX, posY, opacity)

    suspend fun updateStatus(id: Long, isLocked: Boolean, isMinimized: Boolean) =
        noteDao.updateStatus(id, isLocked, isMinimized)
}
