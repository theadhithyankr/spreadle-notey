package com.mininotes.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

enum class SortOrder {
    UPDATED_DESC,
    UPDATED_ASC,
    CREATED_DESC,
    CREATED_ASC,
    ALPHABETICAL_ASC,
    ALPHABETICAL_DESC
}

@Dao
interface NoteDao {
    // Active notes (not deleted, not archived)
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Query("""SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, updatedAt DESC""")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    // Archived notes
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>
    
    // Trash/Deleted notes
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>
    
    // Sorted queries for active notes
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesSortedByUpdatedDesc(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt ASC")
    fun getNotesSortedByUpdatedAsc(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesSortedByCreatedDesc(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, createdAt ASC")
    fun getNotesSortedByCreatedAsc(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, title COLLATE NOCASE ASC")
    fun getNotesSortedByTitleAsc(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, title COLLATE NOCASE DESC")
    fun getNotesSortedByTitleDesc(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?
    
    @Insert
    suspend fun insertNote(note: NoteEntity): Long
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    // Permanent delete
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun permanentlyDeleteNote(noteId: Long)
    
    // Delete old trash items (older than 30 days)
    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt < :timestamp")
    suspend fun deleteOldTrashedNotes(timestamp: Long)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun deleteAllTrash()
    
    // Get all notes for export (including archived but not deleted)
    @Query("SELECT * FROM notes WHERE isDeleted = 0")
    suspend fun getAllNotesForExport(): List<NoteEntity>
}
