package com.mininotes.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [NoteEntity::class], version = 4, exportSchema = false)
@androidx.room.TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                database.execSQL("ALTER TABLE notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notes ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notes ADD COLUMN deletedAt INTEGER")
                database.execSQL("ALTER TABLE notes ADD COLUMN color TEXT")
                database.execSQL("ALTER TABLE notes ADD COLUMN labels TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN richSpansJson TEXT NOT NULL DEFAULT '[]'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN type TEXT NOT NULL DEFAULT 'TEXT'")
                database.execSQL("ALTER TABLE notes ADD COLUMN checklistItems TEXT NOT NULL DEFAULT '[]'")
            }
        }

        fun getDatabase(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
