package com.mininotes.app.domain.usecase

import com.mininotes.app.data.NoteEntity
import com.mininotes.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class SearchNotes(private val repository: NoteRepository) {
    operator fun invoke(query: String): Flow<List<NoteEntity>> {
        return repository.searchNotes(query)
    }
}
