# Mentsic (Metrolist) — App Summary

**Mentsic** (fork/client based on Metrolist) is a feature-rich, third-party **YouTube Music client for Android** built with Kotlin and following Material 3 design guidelines closely.

---

## 🌟 Key Features

### 🎧 Playback & Audio
- **Streaming**: Stream any song or video directly from YouTube Music.
- **Background Playback**: Listen to music while using other apps or when the screen is off.
- **Offline Mode**: Download and cache content for offline listening.
- **Advanced Audio Control**: 
  - Audio normalization
  - Tempo and pitch control
  - Built-in equalizer and crossfade
  - Skip silence feature
- **Playback Tools**: Sleep timer, queue reordering, and local playlist management.

### 📝 Lyrics & Discovery
- **Live Synced Lyrics**: Real-time synchronized lyrics display.
- **Translation**: AI-powered lyrics translation support.
- **Discovery**: Personalized quick picks and comprehensive search functionality for songs, albums, artists, videos, and playlists.

### 🎨 Material 3 Design
- Modern, clean user interface adhering closely to Google's Material 3 design guidelines.
- Fluid transitions, dynamic color support, and dedicated light/dark mode implementations.

---

## 🛠️ Tech Stack & Architecture

### Core Technologies
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: Modular Android app structure (incorporating modules like `app` and `innertube`).
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)

### Key Libraries & Dependencies
- **Dependency Injection**: Hilt (with `@HiltViewModel`, `@AndroidEntryPoint`, `@HiltAndroidApp`)
- **Database**: Room (with multiple entities, DAOs, and migrations)
- **Media Playback**: Media3 (ExoPlayer), with Google Cast support (GMS flavor only)
- **Networking**: Ktor Client (with JSON serialization)
- **Serialization**: Kotlinx Serialization, Protobuf (lite)
- **Image Loading**: Coil
- **Logging**: Timber
- **Background Tasks**: Coroutines, WorkManager
- **UI Enhancements**: Shimmer (skeleton loading), Reorderable (for queue reordering)

### Database Schema (Room)
The app uses a comprehensive Room database with the following entities:
- `SongEntity` - Music tracks
- `ArtistEntity` - Artists
- `AlbumEntity` - Albums
- `PlaylistEntity` - User playlists
- `SongArtistMap`, `SongAlbumMap`, `AlbumArtistMap` - Relationship tables
- `PlaylistSongMap` - Playlist-song associations
- `SearchHistory` - User search history
- `LyricsEntity` - Cached lyrics
- `FormatEntity` - Audio format information
- `Event`, `PlayCountEntity`, `RecognitionHistory` - Analytics and usage tracking
- `PodcastEntity`, `RelatedSongMap`, `SetVideoIdEntity`, `SpeedDialItem` - Additional features

### Product Flavors
The app supports multiple build variants:
- **foss** (default): Open-source only, no Google Cast, with updater
- **gms**: Includes Google Cast support
- **izzyOnDroid**: F-Droid compliant (no cast, no updater)

### Application Information
- **Package Name**: `com.metrolist.music` (configurable via `METROLIST_APPLICATION_ID` env var)
- **App Name**: "Mentsic" (configurable via `METROLIST_APP_NAME` env var)
- **Current Version**: 13.7.0 (versionCode 153)
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 36
- **Compile SDK**: 37

### Key Components

#### Activities
- `MainActivity` - Main entry point with Compose UI, navigation, and player controls
- `DiscordOAuthActivity` - Discord integration OAuth flow
- `RecognitionLaunchActivity` - Music recognition launch
- `CrashActivity` - Crash reporting screen
- `ExoDownloadService` - Download service for offline content
- `MusicService` - MediaLibraryService for background playback
- `PlaylistWidgetReceiver` - Home screen playlist widget

#### ViewModels (Hilt-managed)
- `AccountSettingsViewModel`, `AccountViewModel` - User account management
- `AlbumViewModel`, `ArtistViewModel` - Content display
- `LibraryViewModels` (multiple) - Library management
- `LocalPlaylistViewModel`, `LocalSearchViewModel` - Local content
- `LyricsViewModel`, `LyricsMenuViewModel` - Lyrics display
- `OnlineSearchViewModel`, `OnlineSearchSuggestionViewModel` - Online search
- `OnlinePlaylistViewModel`, `OnlinePodcastViewModel` - Online content
- `PlaylistsViewModel`, `TopPlaylistViewModel` - Playlist management
- `StatsViewModel` - Statistics
- `YouTubeBrowseViewModel` - YouTube browsing
- `EQViewModel`, `WizardViewModel` - Equalizer setup
- `BackupRestoreViewModel` - Data backup/restore
- `MoodAndGenresViewModel`, `NewReleaseViewModel` - Discovery

#### Database DAOs
- `DatabaseDao` - Main database access (extensive query set)
- `SpeedDialDao` - Speed dial shortcuts

#### YouTube Music API (`innertube` module)
- Custom implementation of YouTube Music inner tube API
- Handles authentication, page parsing, and content retrieval
- Models for various YouTube Music renderers (MusicResponsiveHeaderRenderer, MusicPlaylistShelfRenderer, etc.)
- Support for continuations, pagination, and search
- URL parsing utilities

---

## 📱 UI Screens
The app features a complete Material 3 interface with:
- Main navigation (Home, Library, Search, Profile)
- Player screen with full playback controls
- Queue management
- Lyrics display with translation
- Settings (Appearance, Notifications, Audio, Android Auto, Last.fm integration, etc.)
- Equalizer with presets and custom profiles
- Backup/restore functionality
- Discord rich presence integration
- Crash reporting

---

*Updated for the Mentsic project repository.*