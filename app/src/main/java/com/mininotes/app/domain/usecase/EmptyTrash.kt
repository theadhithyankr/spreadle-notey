package com.mininotes.app.domain.usecase

import com.mininotes.app.domain.repository.NoteRepository

class EmptyTrash(private val repository: NoteRepository) {
    suspend operator fun invoke() {
        repository.emptyTrash()
    }
}
