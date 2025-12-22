# Mini Notes

A minimal, offline-first, privacy-focused notes application for Android.

## Features

- ✅ Create, edit, and delete notes
- ✅ Auto-save while typing
- ✅ Offline-first (no network access)
- ✅ Clean Material 3 design
- ✅ Dark mode support
- ✅ Zero tracking, zero data collection

## Privacy

**All data stays on your device.** This app:

- Does not access the internet
- Does not collect any data
- Does not require any permissions
- Does not include ads or analytics
- Does not use Google Play Services

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM
- **Database:** Room (SQLite)
- **Async:** Kotlin Coroutines + Flow
- **Navigation:** Jetpack Navigation Compose
- **Min SDK:** 26 (Android 8.0+)

## Building

1. Clone the repository
2. Open in Android Studio
3. Build and run

```bash
./gradlew assembleDebug
```

## Project Structure

```
com.mininotes.app
│
├── data                    # Data layer
│   ├── NoteEntity.kt       # Room entity
│   ├── NoteDao.kt          # Database access object
│   └── NotesDatabase.kt    # Room database
│
├── ui
│   ├── notes               # Notes list screen
│   │   ├── NotesScreen.kt
│   │   └── NotesViewModel.kt
│   │
│   ├── editor              # Note editor screen
│   │   ├── EditorScreen.kt
│   │   └── EditorViewModel.kt
│   │
│   └── theme
│       └── Theme.kt
│
├── navigation
│   └── NavGraph.kt         # Navigation setup
│
├── MainActivity.kt         # Entry point
└── NotesApplication.kt     # Application class
```

## License

Apache License 2.0 - see [LICENSE](LICENSE) file for details.

## Contributing

This is a learning/portfolio project. Feel free to fork and modify for your own use.

## F-Droid

This app is designed to be published on F-Droid. All dependencies are open-source and FOSS-compatible.
