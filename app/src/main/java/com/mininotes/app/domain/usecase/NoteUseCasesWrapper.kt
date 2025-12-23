package com.mininotes.app.domain.usecase

data class NoteUseCases(
    val getNotes: GetNotes,
    val getArchivedNotes: GetArchivedNotes,
    val searchNotes: SearchNotes,
    val deleteNote: DeleteNote,
    val addNote: AddNote,
    val getNote: GetNote,
    val restoreNote: RestoreNote,
    val permanentlyDeleteNote: PermanentlyDeleteNote,
    val getDeletedNotes: GetDeletedNotes,
    val emptyTrash: EmptyTrash
)
