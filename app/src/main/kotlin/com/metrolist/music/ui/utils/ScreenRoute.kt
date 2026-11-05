/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.utils

import com.metrolist.music.R

/**
 * Type-safe navigation routes replacing raw string routes throughout the app.
 * Each route maps to its string value used in [androidx.navigation.NavController].
 */
sealed class ScreenRoute(val route: String) {
    object Home : ScreenRoute("home")
    object Search : ScreenRoute("search_input")
    object ListenTogether : ScreenRoute("listen_together")
    object Library : ScreenRoute("library")

    object Settings : ScreenRoute("settings")
    object Player : ScreenRoute("player")
    object Album : ScreenRoute("album")
    object Artist : ScreenRoute("artist")
    object Playlist : ScreenRoute("playlist")
    object Stats : ScreenRoute("stats")
    object Recognition : ScreenRoute("recognition")
    object Charts : ScreenRoute("charts")
    object History : ScreenRoute("history")
    object LocalSearch : ScreenRoute("local_search")
    object OnlineSearch : ScreenRoute("online_search")
    object YouTubeBrowse : ScreenRoute("youtube_browse")
    object About : ScreenRoute("about")
    object AccountSettings : ScreenRoute("account_settings")
    object AiSettings : ScreenRoute("ai_settings")
    object AlarmSettings : ScreenRoute("alarm_settings")
    object AndroidAutoSettings : ScreenRoute("android_auto_settings")
    object AppearanceSettings : ScreenRoute("appearance_settings")
    object BackupAndRestore : ScreenRoute("backup_and_restore")
    object ChangelogScreen : ScreenRoute("changelog")
    object ContentSettings : ScreenRoute("content_settings")
    object DiscordSettings : ScreenRoute("discord_settings")
    object IntegrationScreen : ScreenRoute("integration")
    object LastFMSettings : ScreenRoute("lastfm_settings")
    object ListenTogetherSettings : ScreenRoute("listen_together_settings")
    object PlayerSettings : ScreenRoute("player_settings")
    object PrivacySettings : ScreenRoute("privacy_settings")
    object RomanizationSettings : ScreenRoute("romanization_settings")
    object StorageSettings : ScreenRoute("storage_settings")
    object ThemeScreen : ScreenRoute("theme")
    object UpdaterSettings : ScreenRoute("updater")
    object RecognitionHistoryScreen : ScreenRoute("recognition_history")
    object SearchScreen : ScreenRoute("search")
    object LocalSearchScreen : ScreenRoute("local_search_screen")
    object OnlineSearchScreen : ScreenRoute("online_search_screen")
    object OnlineSearchResult : ScreenRoute("online_search_result")
    object HomeScreen : ScreenRoute("home_screen")

    companion object {
        /** All main navigation bar screens */
        val MainScreens = listOf(Home, Search, ListenTogether, Library)

        /**
         * Parses a string route into its corresponding [ScreenRoute], if known.
         */
        fun fromString(route: String): ScreenRoute? {
            return when (route) {
                "home" -> Home
                "search_input" -> Search
                "listen_together" -> ListenTogether
                "library" -> Library
                "settings" -> Settings
                "player" -> Player
                "album" -> Album
                "artist" -> Artist
                "playlist" -> Playlist
                "stats" -> Stats
                "recognition" -> Recognition
                "charts" -> Charts
                "history" -> History
                "local_search" -> LocalSearch
                "online_search" -> OnlineSearch
                "youtube_browse" -> YouTubeBrowse
                "about" -> About
                "account_settings" -> AccountSettings
                "ai_settings" -> AiSettings
                "alarm_settings" -> AlarmSettings
                "android_auto_settings" -> AndroidAutoSettings
                "appearance_settings" -> AppearanceSettings
                "backup_and_restore" -> BackupAndRestore
                "changelog" -> ChangelogScreen
                "content_settings" -> ContentSettings
                "discord_settings" -> DiscordSettings
                "integration" -> IntegrationScreen
                "lastfm_settings" -> LastFMSettings
                "listen_together_settings" -> ListenTogetherSettings
                "player_settings" -> PlayerSettings
                "privacy_settings" -> PrivacySettings
                "romanization_settings" -> RomanizationSettings
                "storage_settings" -> StorageSettings
                "theme" -> ThemeScreen
                "updater" -> UpdaterSettings
                "recognition_history" -> RecognitionHistoryScreen
                "search" -> SearchScreen
                "local_search_screen" -> LocalSearchScreen
                "online_search_screen" -> OnlineSearchScreen
                "online_search_result" -> OnlineSearchResult
                "home_screen" -> HomeScreen
                else -> null
            }
        }
    }
}