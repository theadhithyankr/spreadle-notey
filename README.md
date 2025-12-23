![1766405189662](image/README/1766405189662.png)# Mini Notes

A feature-rich, offline-first, privacy-focused notes application for Android.

## ✨ Features

### Core Functionality
- ✅ Create, edit, and delete notes with auto-save
- ✅ **Search** - Full-text search with highlighting
- ✅ **Sort Options** - Sort by updated/created date or title (A-Z)
- ✅ **Pin Notes** - Keep important notes at the top
- ✅ **Color Coding** - Organize notes with custom colors
- ✅ **Labels/Tags** - Add hashtags for categorization

### Organization
- ✅ **Archive** - Hide old notes without deleting
- ✅ **Trash System** - Soft delete with 30-day auto-cleanup
- ✅ **Restore** - Recover deleted notes from trash

### Editing & Formatting
- ✅ **Markdown Support** - Write with **bold**, *italic*, # headings, - lists
- ✅ **Formatting Toolbar** - Quick markdown insertion
- ✅ **Word/Character Count** - Real-time statistics
- ✅ **Share Notes** - Share to any app via Android share sheet

### Design
- ✅ Clean Material 3 design
- ✅ Light & dark mode support
- ✅ Dynamic color theming (Android 12+)
- ✅ Smooth animations and transitions

## 🔒 Privacy

**All data stays on your device.** This app:
- ❌ No internet access
- ❌ No data collection
- ❌ No permissions required
- ❌ No ads or analytics
- ❌ No Google Play Services

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM
- **Database:** Room (SQLite) v2 with migration support
- **Async:** Kotlin Coroutines + StateFlow
- **Navigation:** Jetpack Navigation Compose
- **Work:** WorkManager for background cleanup
- **Min SDK:** 26 (Android 8.0+)

## 📱 Usage

### Notes List
- **Tap +** to create a new note
- **Tap note** to edit
- **Long press** to move to trash
- **Search icon** to find notes
- **Sort icon (⇅)** to change order
- **Menu (⋮)** to access Archive and Trash

### Editor
- **Share icon** to share note
- **Menu (⋮)** for pin, archive, and color options
- **Formatting buttons** to insert markdown
- **Word count** displayed at bottom

### Trash
- **Tap note** to restore
- **Long press** to delete permanently
- Notes auto-delete after 30 days

## 🚀 Building

1. Clone the repository
2. Open in Android Studio
3. Build and run

```bash
./gradlew assembleDebug
```

## 📦 Project Structure

```
com.mininotes.app
│
├── data                        # Data layer
│   ├── NoteEntity.kt           # Room entity with full schema
│   ├── NoteDao.kt              # Comprehensive queries
│   └── NotesDatabase.kt        # Database with v1→v2 migration
│
├── ui
│   ├── notes                   # Notes list with search/sort/filter
│   │   ├── NotesScreen.kt
│   │   └── NotesViewModel.kt
│   │
│   ├── editor                  # Rich note editor
│   │   ├── EditorScreen.kt
│   │   └── EditorViewModel.kt
│   │
│   ├── trash                   # Trash management
│   │   ├── TrashScreen.kt
│   │   └── TrashViewModel.kt
│   │
│   └── theme
│       └── Theme.kt
│
├── navigation
│   └── NavGraph.kt
│
├── MainActivity.kt
└── NotesApplication.kt
```

## 📄 License

Apache License 2.0 - see [LICENSE](LICENSE) file for details.

## 🤝 Contributing

This is a learning/portfolio project demonstrating modern Android development. Feel free to fork and modify for your own use.

## 📲 F-Droid

This app is designed to be published on F-Droid. All dependencies are FOSS-compatible:
- ✅ Room, Compose, Material 3 (Apache 2.0)
- ✅ WorkManager, DataStore (Apache 2.0)
- ✅ Kotlinx Serialization (Apache 2.0)
- ✅ No proprietary libraries
- ✅ No network dependencies

## 🎯 Feature Roadmap (Future)

- [ ] Export/Import notes as JSON
- [ ] Biometric lock option
- [ ] Rich text editor with visual formatting
- [ ] Swipe gestures for quick actions
- [ ] Widget for home screen
- [ ] Note templates

---

**Version:** 2.0.0  
**Database Version:** 2  
**Last Updated:** December 2025
