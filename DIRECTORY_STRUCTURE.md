# Metrolist — Directory Structure

Comprehensive map of repository layout, excluding build artifacts and IDE-specific files.

Generated from working tree. Directories end with `/`.

## Major Areas

- **.github/** — CI/CD workflows, issue/PR templates, helper scripts.
- **app/** — Main Android application module.
- **assets/** — Static assets (badges).
- **fastlane/** — Fastlane metadata/store listings.
- **gradle/** — Gradle wrapper.
- **innertube/** — InnerTube API client module.
- **tests/** — Standalone test harness/scripts.

## Root

Root folder: **`Mentsic`** (`d:\Mentsic`)

```
.github/
app/
assets/
fastlane/
gradle/
innertube/
tests/
.gitignore
AGENTS.md
LICENSE
OrbitTheme.md
README.md
build.gradle.kts
changelog.md
crowdin.yml
development_guide.md
gradle.properties
gradlew
gradlew.bat
lint.xml
renovate.json
rules.md
settings.gradle.kts
ui_rulebook.md
```

## .github

CI/CD workflows, issue/PR templates, helper scripts.

```
.github/
    ├── ISSUE_TEMPLATE/
    │   ├── bug_report.yml
    │   ├── config.yml
    │   └── feature_request.yml
    ├── scripts/
    │   └── parse_changelog.sh
    ├── workflows/
    │   ├── build.yml
    │   ├── build_pr.yml
    │   ├── build_quick.yml
    │   └── release.yml
    ├── FUNDING.yml
    └── pull_request_template.md
```

## app

Main Android application module.

```
app/
    ├── schemas/
    │   └── com.metrolist.music.db.InternalDatabase/
    │       ├── 1.json
    │       ├── 10.json
    │       ├── 11.json
    │       ├── 12.json
    │       ├── 13.json
    │       ├── 14.json
    │       ├── 15.json
    │       ├── 16.json
    │       ├── 17.json
    │       ├── 18.json
    │       ├── 19.json
    │       ├── 2.json
    │       ├── 20.json
    │       ├── 21.json
    │       ├── 22.json
    │       ├── 23.json
    │       ├── 24.json
    │       ├── 25.json
    │       ├── 26.json
    │       ├── 27.json
    │       ├── 28.json
    │       ├── 29.json
    │       ├── 3.json
    │       ├── 30.json
    │       ├── 31.json
    │       ├── 32.json
    │       ├── 33.json
    │       ├── 34.json
    │       ├── 35.json
    │       ├── 36.json
    │       ├── 37.json
    │       ├── 38.json
    │       ├── 4.json
    │       ├── 5.json
    │       ├── 6.json
    │       ├── 7.json
    │       ├── 8.json
    │       └── 9.json
    ├── src/
    │   ├── debug/
    │   │   └── res/
    │   │       └── xml/
    │   │           └── shortcuts.xml
    │   ├── foss/
    │   │   ├── kotlin/
    │   │   │   └── com/
    │   │   │       └── metrolist/
    │   │   │           └── music/
    │   │   │               ├── cast/
    │   │   │               ├── playback/
    │   │   │               └── ui/
    │   │   │                   └── component/
    │   │   └── AndroidManifest.xml
    │   ├── gms/
    │   │   └── kotlin/
    │   │       └── com/
    │   │           └── metrolist/
    │   │               └── music/
    │   │                   ├── cast/
    │   │                   │   └── CastOptionsProvider.kt
    │   │                   ├── playback/
    │   │                   │   └── CastConnectionHandler.kt
    │   │                   └── ui/
    │   │                       └── component/
    │   │                           └── CastButton.kt
    │   ├── izzy/
    │   │   ├── kotlin/
    │   │   │   └── com/
    │   │   │       └── metrolist/
    │   │   │           └── music/
    │   │   │               ├── cast/
    │   │   │               ├── playback/
    │   │   │               └── ui/
    │   │   │                   └── component/
    │   │   └── AndroidManifest.xml
    │   ├── main/
    │   │   ├── assets/
    │   │   │   ├── solver/
    │   │   │   │   ├── astring.js
    │   │   │   │   ├── meriyah.js
    │   │   │   │   └── yt.solver.core.js
    │   │   │   └── po_token.html
    │   │   ├── kotlin/
    │   │   │   └── com/
    │   │   │       └── metrolist/
    │   │   │           ├── kugou/
    │   │   │           │   ├── models/
    │   │   │           │   │   ├── DownloadLyricsResponse.kt
    │   │   │           │   │   ├── Keyword.kt
    │   │   │           │   │   ├── SearchLyricsResponse.kt
    │   │   │           │   │   └── SearchSongResponse.kt
    │   │   │           │   └── KuGou.kt
    │   │   │           ├── lastfm/
    │   │   │           │   ├── models/
    │   │   │           │   │   └── Authentication.kt
    │   │   │           │   └── LastFM.kt
    │   │   │           ├── lrclib/
    │   │   │           │   ├── models/
    │   │   │           │   │   └── Track.kt
    │   │   │           │   └── LrcLib.kt
    │   │   │           ├── music/
    │   │   │           │   ├── api/
    │   │   │           │   │   ├── DeepLService.kt
    │   │   │           │   │   ├── OpenRouterService.kt
    │   │   │           │   │   └── OpenRouterStreamingService.kt
    │   │   │           │   ├── betterlyrics/
    │   │   │           │   │   ├── models/
    │   │   │           │   │   │   └── Track.kt
    │   │   │           │   │   ├── BetterLyrics.kt
    │   │   │           │   │   └── TTMLParser.kt
    │   │   │           │   ├── constants/
    │   │   │           │   │   ├── Dimensions.kt
    │   │   │           │   │   ├── HistorySource.kt
    │   │   │           │   │   ├── LibraryFilter.kt
    │   │   │           │   │   ├── MediaSessionConstants.kt
    │   │   │           │   │   ├── PreferenceKeys.kt
    │   │   │           │   │   └── StatPeriod.kt
    │   │   │           │   ├── db/
    │   │   │           │   │   ├── daos/
    │   │   │           │   │   │   └── SpeedDialDao.kt
    │   │   │           │   │   ├── entities/
    │   │   │           │   │   │   ├── Album.kt
    │   │   │           │   │   │   ├── AlbumArtistMap.kt
    │   │   │           │   │   │   ├── AlbumEntity.kt
    │   │   │           │   │   │   ├── AlbumWithSongs.kt
    │   │   │           │   │   │   ├── Artist.kt
    │   │   │           │   │   │   ├── ArtistEntity.kt
    │   │   │           │   │   │   ├── ArtistPageCache.kt
    │   │   │           │   │   │   ├── DailyPlayCount.kt
    │   │   │           │   │   │   ├── Event.kt
    │   │   │           │   │   │   ├── EventWithSong.kt
    │   │   │           │   │   │   ├── FormatEntity.kt
    │   │   │           │   │   │   ├── LocalItem.kt
    │   │   │           │   │   │   ├── LyricsEntity.kt
    │   │   │           │   │   │   ├── PlayCountEntity.kt
    │   │   │           │   │   │   ├── Playlist.kt
    │   │   │           │   │   │   ├── PlaylistEntity.kt
    │   │   │           │   │   │   ├── PlaylistSong.kt
    │   │   │           │   │   │   ├── PlaylistSongMap.kt
    │   │   │           │   │   │   ├── PlaylistSongMapPreview.kt
    │   │   │           │   │   │   ├── PodcastEntity.kt
    │   │   │           │   │   │   ├── RecognitionHistory.kt
    │   │   │           │   │   │   ├── RelatedSongMap.kt
    │   │   │           │   │   │   ├── SearchHistory.kt
    │   │   │           │   │   │   ├── SetVideoIdEntity.kt
    │   │   │           │   │   │   ├── Song.kt
    │   │   │           │   │   │   ├── SongAlbumMap.kt
    │   │   │           │   │   │   ├── SongArtistMap.kt
    │   │   │           │   │   │   ├── SongEntity.kt
    │   │   │           │   │   │   ├── SongWithStats.kt
    │   │   │           │   │   │   ├── SortedSongAlbumMap.kt
    │   │   │           │   │   │   ├── SortedSongArtistMap.kt
    │   │   │           │   │   │   └── SpeedDialItem.kt
    │   │   │           │   │   ├── Converters.kt
    │   │   │           │   │   ├── DatabaseDao.kt
    │   │   │           │   │   └── MusicDatabase.kt
    │   │   │           │   ├── di/
    │   │   │           │   │   ├── AppModule.kt
    │   │   │           │   │   ├── LyricsHelperEntryPoint.kt
    │   │   │           │   │   ├── NetworkModule.kt
    │   │   │           │   │   └── Qualifiers.kt
    │   │   │           │   ├── discord/
    │   │   │           │   │   ├── DiscordActivity.kt
    │   │   │           │   │   ├── DiscordActivityBuilder.kt
    │   │   │           │   │   ├── DiscordAuth.kt
    │   │   │           │   │   ├── DiscordDefaults.kt
    │   │   │           │   │   ├── DiscordExternalAssets.kt
    │   │   │           │   │   ├── DiscordGateway.kt
    │   │   │           │   │   ├── DiscordOAuthActivity.kt
    │   │   │           │   │   ├── DiscordPresence.kt
    │   │   │           │   │   ├── DiscordReconnectStrategy.kt
    │   │   │           │   │   ├── DiscordRpcManager.kt
    │   │   │           │   │   ├── DiscordSuperProperties.kt
    │   │   │           │   │   ├── DiscordTemplateRenderer.kt
    │   │   │           │   │   └── DiscordTokenStore.kt
    │   │   │           │   ├── eq/
    │   │   │           │   │   ├── audio/
    │   │   │           │   │   │   ├── BiquadFilter.kt
    │   │   │           │   │   │   └── CustomEqualizerAudioProcessor.kt
    │   │   │           │   │   ├── data/
    │   │   │           │   │   │   ├── EQProfileRepository.kt
    │   │   │           │   │   │   ├── Entry.kt
    │   │   │           │   │   │   ├── FilterType.kt
    │   │   │           │   │   │   ├── GitHubAutoEqSearch.kt
    │   │   │           │   │   │   ├── ParametricEQ.kt
    │   │   │           │   │   │   └── ParametricEQParser.kt
    │   │   │           │   │   └── EqualizerService.kt
    │   │   │           │   ├── extensions/
    │   │   │           │   │   ├── ContentResolverExt.kt
    │   │   │           │   │   ├── ContextExt.kt
    │   │   │           │   │   ├── CoroutineExt.kt
    │   │   │           │   │   ├── FileExt.kt
    │   │   │           │   │   ├── ListExt.kt
    │   │   │           │   │   ├── MediaItemExt.kt
    │   │   │           │   │   ├── PlayerExt.kt
    │   │   │           │   │   ├── QueueExt.kt
    │   │   │           │   │   ├── StringExt.kt
    │   │   │           │   │   └── UtilExt.kt
    │   │   │           │   ├── listentogether/
    │   │   │           │   │   ├── ListenTogetherActionReceiver.kt
    │   │   │           │   │   ├── ListenTogetherClient.kt
    │   │   │           │   │   ├── ListenTogetherManager.kt
    │   │   │           │   │   ├── ListenTogetherServers.kt
    │   │   │           │   │   ├── MessageCodec.kt
    │   │   │           │   │   ├── Protocol.kt
    │   │   │           │   │   └── ServerClock.kt
    │   │   │           │   ├── lyrics/
    │   │   │           │   │   ├── BetterLyricsProvider.kt
    │   │   │           │   │   ├── KuGouLyricsProvider.kt
    │   │   │           │   │   ├── LrcLibLyricsProvider.kt
    │   │   │           │   │   ├── LyricsEntry.kt
    │   │   │           │   │   ├── LyricsFormat.kt
    │   │   │           │   │   ├── LyricsHelper.kt
    │   │   │           │   │   ├── LyricsPlusProvider.kt
    │   │   │           │   │   ├── LyricsProvider.kt
    │   │   │           │   │   ├── LyricsProviderRegistry.kt
    │   │   │           │   │   ├── LyricsTranslationHelper.kt
    │   │   │           │   │   ├── LyricsUtils.kt
    │   │   │           │   │   ├── PaxsenixLyricsProvider.kt
    │   │   │           │   │   ├── YouTubeLyricsProvider.kt
    │   │   │           │   │   ├── YouTubeSubtitleLyricsProvider.kt
    │   │   │           │   │   └── ZemerLyricsProvider.kt
    │   │   │           │   ├── models/
    │   │   │           │   │   ├── ItemsPage.kt
    │   │   │           │   │   ├── MediaMetadata.kt
    │   │   │           │   │   ├── PersistPlayerState.kt
    │   │   │           │   │   ├── PersistQueue.kt
    │   │   │           │   │   └── SimilarRecommendation.kt
    │   │   │           │   ├── playback/
    │   │   │           │   │   ├── alarm/
    │   │   │           │   │   │   ├── MusicAlarmReceiver.kt
    │   │   │           │   │   │   ├── MusicAlarmRescheduleReceiver.kt
    │   │   │           │   │   │   ├── MusicAlarmScheduler.kt
    │   │   │           │   │   │   └── MusicAlarmStore.kt
    │   │   │           │   │   ├── audio/
    │   │   │           │   │   │   ├── SilenceDetectorAudioProcessor.kt
    │   │   │           │   │   │   └── VolumeNormalizationAudioProcessor.kt
    │   │   │           │   │   ├── queues/
    │   │   │           │   │   │   ├── EmptyQueue.kt
    │   │   │           │   │   │   ├── ListQueue.kt
    │   │   │           │   │   │   ├── LocalAlbumRadio.kt
    │   │   │           │   │   │   ├── Queue.kt
    │   │   │           │   │   │   ├── YouTubeAlbumRadio.kt
    │   │   │           │   │   │   ├── YouTubePlaylistQueue.kt
    │   │   │           │   │   │   └── YouTubeQueue.kt
    │   │   │           │   │   ├── CastConnectionHandler.kt
    │   │   │           │   │   ├── DownloadUtil.kt
    │   │   │           │   │   ├── ExoDownloadService.kt
    │   │   │           │   │   ├── MediaLibrarySessionCallback.kt
    │   │   │           │   │   ├── MusicService.kt
    │   │   │           │   │   ├── PlayerConnection.kt
    │   │   │           │   │   ├── SleepTimer.kt
    │   │   │           │   │   ├── StreamUrlCache.kt
    │   │   │           │   │   └── VoiceSearchMatcher.kt
    │   │   │           │   ├── quicksettings/
    │   │   │           │   ├── recognition/
    │   │   │           │   │   ├── AudioResampler.kt
    │   │   │           │   │   ├── MusicRecognitionService.kt
    │   │   │           │   │   ├── RecognitionForegroundService.kt
    │   │   │           │   │   ├── RecognitionLaunchActivity.kt
    │   │   │           │   │   ├── ShazamSignatureGenerator.kt
    │   │   │           │   │   └── VibraSignature.kt
    │   │   │           │   ├── ui/
    │   │   │           │   │   ├── component/
    │   │   │           │   │   │   ├── shimmer/
    │   │   │           │   │   │   │   ├── ShimmerHost.kt
    │   │   │           │   │   │   │   ├── ShimmerPlaceholders.kt
    │   │   │           │   │   │   │   ├── ShimmerTheme.kt
    │   │   │           │   │   │   │   └── TextPlaceholder.kt
    │   │   │           │   │   │   ├── AppNavigation.kt
    │   │   │           │   │   │   ├── AudioVisualizer.kt
    │   │   │           │   │   │   ├── BottomSheet.kt
    │   │   │           │   │   │   ├── BottomSheetMenu.kt
    │   │   │           │   │   │   ├── BottomSheetPage.kt
    │   │   │           │   │   │   ├── CastButton.kt
    │   │   │           │   │   │   ├── ChipsRow.kt
    │   │   │           │   │   │   ├── CreatePlaylistDialog.kt
    │   │   │           │   │   │   ├── Dialog.kt
    │   │   │           │   │   │   ├── DraggableLyricsProviderList.kt
    │   │   │           │   │   │   ├── DraggableScrollBarOverlay.kt
    │   │   │           │   │   │   ├── EmptyPlaceholder.kt
    │   │   │           │   │   │   ├── EnumDialog.kt
    │   │   │           │   │   │   ├── ExpandableText.kt
    │   │   │           │   │   │   ├── ExperimentalLyrics.kt
    │   │   │           │   │   │   ├── HideOnScrollFAB.kt
    │   │   │           │   │   │   ├── IconButton.kt
    │   │   │           │   │   │   ├── IntegrationCard.kt
    │   │   │           │   │   │   ├── Items.kt
    │   │   │           │   │   │   ├── Library.kt
    │   │   │           │   │   │   ├── LibrarySearchHeader.kt
    │   │   │           │   │   │   ├── Lyrics.kt
    │   │   │           │   │   │   ├── LyricsBackgroundStyle.kt
    │   │   │           │   │   │   ├── LyricsCommon.kt
    │   │   │           │   │   │   ├── LyricsComponents.kt
    │   │   │           │   │   │   ├── LyricsImageCard.kt
    │   │   │           │   │   │   ├── LyricsLine.kt
    │   │   │           │   │   │   ├── Material3SettingsGroup.kt
    │   │   │           │   │   │   ├── Menu.kt
    │   │   │           │   │   │   ├── NavigationTitle.kt
    │   │   │           │   │   │   ├── NewMenuComponents.kt
    │   │   │           │   │   │   ├── OriginalLyrics.kt
    │   │   │           │   │   │   ├── PlayerSlider.kt
    │   │   │           │   │   │   ├── PlayingIndicator.kt
    │   │   │           │   │   │   ├── RandomizeGridItem.kt
    │   │   │           │   │   │   ├── ReleaseNotesCard.kt
    │   │   │           │   │   │   ├── SettingsSleepTimerDialog.kt
    │   │   │           │   │   │   ├── SongDropdownSelect.kt
    │   │   │           │   │   │   ├── SortHeader.kt
    │   │   │           │   │   │   ├── SpeedDialGridItem.kt
    │   │   │           │   │   │   ├── SquigglySlider.kt
    │   │   │           │   │   │   ├── TacticalHub.kt
    │   │   │           │   │   │   ├── TimeTransfer.kt
    │   │   │           │   │   │   ├── VolumeSlider.kt
    │   │   │           │   │   │   └── WavySlider.kt
    │   │   │           │   │   ├── menu/
    │   │   │           │   │   │   ├── AddToPlaylistDialog.kt
    │   │   │           │   │   │   ├── AddToPlaylistDialogOnline.kt
    │   │   │           │   │   │   ├── AlbumMenu.kt
    │   │   │           │   │   │   ├── ArtistMenu.kt
    │   │   │           │   │   │   ├── CsvColumnMappingDialog.kt
    │   │   │           │   │   │   ├── CustomThumbnailMenu.kt
    │   │   │           │   │   │   ├── ImportPlaylistDialog.kt
    │   │   │           │   │   │   ├── LoadingScreen.kt
    │   │   │           │   │   │   ├── LyricsMenu.kt
    │   │   │           │   │   │   ├── PlayerMenu.kt
    │   │   │           │   │   │   ├── PlaylistMenu.kt
    │   │   │           │   │   │   ├── PlaylistScreenMenus.kt
    │   │   │           │   │   │   ├── QueueMenu.kt
    │   │   │           │   │   │   ├── SelectionSongsMenu.kt
    │   │   │           │   │   │   ├── SongMenu.kt
    │   │   │           │   │   │   ├── YouTubeAlbumMenu.kt
    │   │   │           │   │   │   ├── YouTubeArtistMenu.kt
    │   │   │           │   │   │   ├── YouTubePlaylistMenu.kt
    │   │   │           │   │   │   ├── YouTubeSelectionSongMenu.kt
    │   │   │           │   │   │   └── YouTubeSongMenu.kt
    │   │   │           │   │   ├── player/
    │   │   │           │   │   │   ├── MiniPlayer.kt
    │   │   │           │   │   │   ├── PlaybackError.kt
    │   │   │           │   │   │   ├── Player.kt
    │   │   │           │   │   │   ├── Queue.kt
    │   │   │           │   │   │   ├── Thumbnail.kt
    │   │   │           │   │   │   └── ThumbnailSnapUtils.kt
    │   │   │           │   │   ├── screens/
    │   │   │           │   │   │   ├── artist/
    │   │   │           │   │   │   │   ├── ArtistAlbumsScreen.kt
    │   │   │           │   │   │   │   ├── ArtistItemsScreen.kt
    │   │   │           │   │   │   │   ├── ArtistScreen.kt
    │   │   │           │   │   │   │   └── ArtistSongsScreen.kt
    │   │   │           │   │   │   ├── equalizer/
    │   │   │           │   │   │   │   ├── wizard/
    │   │   │           │   │   │   │   │   ├── WizardScreen.kt
    │   │   │           │   │   │   │   │   ├── WizardState.kt
    │   │   │           │   │   │   │   │   └── WizardViewModel.kt
    │   │   │           │   │   │   │   ├── EQState.kt
    │   │   │           │   │   │   │   ├── EQViewModel.kt
    │   │   │           │   │   │   │   ├── EqFrequencyResponseGraph.kt
    │   │   │           │   │   │   │   └── EqScreen.kt
    │   │   │           │   │   │   ├── library/
    │   │   │           │   │   │   │   ├── LibraryAlbumsScreen.kt
    │   │   │           │   │   │   │   ├── LibraryArtistsScreen.kt
    │   │   │           │   │   │   │   ├── LibraryMixScreen.kt
    │   │   │           │   │   │   │   ├── LibraryPlaylistsScreen.kt
    │   │   │           │   │   │   │   ├── LibraryPodcastsScreen.kt
    │   │   │           │   │   │   │   ├── LibraryScreen.kt
    │   │   │           │   │   │   │   └── LibrarySongsScreen.kt
    │   │   │           │   │   │   ├── playlist/
    │   │   │           │   │   │   │   ├── AutoPlaylistScreen.kt
    │   │   │           │   │   │   │   ├── CachePlaylistScreen.kt
    │   │   │           │   │   │   │   ├── LocalPlaylistScreen.kt
    │   │   │           │   │   │   │   ├── OnlinePlaylistScreen.kt
    │   │   │           │   │   │   │   └── TopPlaylistScreen.kt
    │   │   │           │   │   │   ├── podcast/
    │   │   │           │   │   │   │   └── OnlinePodcastScreen.kt
    │   │   │           │   │   │   ├── recognition/
    │   │   │           │   │   │   │   ├── RecognitionHistoryScreen.kt
    │   │   │           │   │   │   │   └── RecognitionScreen.kt
    │   │   │           │   │   │   ├── search/
    │   │   │           │   │   │   │   ├── LocalSearchScreen.kt
    │   │   │           │   │   │   │   ├── OnlineSearchResult.kt
    │   │   │           │   │   │   │   ├── OnlineSearchScreen.kt
    │   │   │           │   │   │   │   └── SearchScreen.kt
    │   │   │           │   │   │   ├── settings/
    │   │   │           │   │   │   │   ├── integrations/
    │   │   │           │   │   │   │   │   ├── DiscordSettings.kt
    │   │   │           │   │   │   │   │   ├── IntegrationScreen.kt
    │   │   │           │   │   │   │   │   ├── LastFMSettings.kt
    │   │   │           │   │   │   │   │   └── ListenTogetherSettings.kt
    │   │   │           │   │   │   │   ├── AboutScreen.kt
    │   │   │           │   │   │   │   ├── AccountSettings.kt
    │   │   │           │   │   │   │   ├── AiSettings.kt
    │   │   │           │   │   │   │   ├── AlarmSettings.kt
    │   │   │           │   │   │   │   ├── AndroidAutoSettings.kt
    │   │   │           │   │   │   │   ├── AppearanceSettings.kt
    │   │   │           │   │   │   │   ├── BackupAndRestore.kt
    │   │   │           │   │   │   │   ├── ChangelogScreen.kt
    │   │   │           │   │   │   │   ├── ContentSettings.kt
    │   │   │           │   │   │   │   ├── PlayerSettings.kt
    │   │   │           │   │   │   │   ├── PrivacySettings.kt
    │   │   │           │   │   │   │   ├── RomanizationSettings.kt
    │   │   │           │   │   │   │   ├── SettingsScreen.kt
    │   │   │           │   │   │   │   ├── StorageSettings.kt
    │   │   │           │   │   │   │   ├── ThemeScreen.kt
    │   │   │           │   │   │   │   └── UpdaterSettings.kt
    │   │   │           │   │   │   ├── AccountScreen.kt
    │   │   │           │   │   │   ├── AlbumScreen.kt
    │   │   │           │   │   │   ├── BrowseScreen.kt
    │   │   │           │   │   │   ├── ChartsScreen.kt
    │   │   │           │   │   │   ├── CrashActivity.kt
    │   │   │           │   │   │   ├── HistoryScreen.kt
    │   │   │           │   │   │   ├── HomeScreen.kt
    │   │   │           │   │   │   ├── HomeSection.kt
    │   │   │           │   │   │   ├── HomeSections.kt
    │   │   │           │   │   │   ├── ListenTogetherScreen.kt
    │   │   │           │   │   │   ├── LoginScreen.kt
    │   │   │           │   │   │   ├── MoodAndGenresScreen.kt
    │   │   │           │   │   │   ├── NavigationBuilder.kt
    │   │   │           │   │   │   ├── NewReleaseScreen.kt
    │   │   │           │   │   │   ├── Screens.kt
    │   │   │           │   │   │   ├── StatsScreen.kt
    │   │   │           │   │   │   └── YouTubeBrowseScreen.kt
    │   │   │           │   │   ├── theme/
    │   │   │           │   │   │   ├── Alphas.kt
    │   │   │           │   │   │   ├── Font.kt
    │   │   │           │   │   │   ├── IconSizes.kt
    │   │   │           │   │   │   ├── OrbitTheme.kt
    │   │   │           │   │   │   ├── PlayerColorExtractor.kt
    │   │   │           │   │   │   ├── PlayerSliderColors.kt
    │   │   │           │   │   │   ├── Spacing.kt
    │   │   │           │   │   │   └── Theme.kt
    │   │   │           │   │   └── utils/
    │   │   │           │   │       ├── AppBar.kt
    │   │   │           │   │       ├── FadingEdge.kt
    │   │   │           │   │       ├── LazyGridSnapLayoutInfoProvider.kt
    │   │   │           │   │       ├── NavControllerUtils.kt
    │   │   │           │   │       ├── ScrollUtils.kt
    │   │   │           │   │       ├── ShowMediaInfo.kt
    │   │   │           │   │       ├── ShowOffsetDialog.kt
    │   │   │           │   │       ├── StringUtils.kt
    │   │   │           │   │       └── YouTubeUtils.kt
    │   │   │           │   ├── utils/
    │   │   │           │   │   ├── potoken/
    │   │   │           │   │   │   ├── JavaScriptUtil.kt
    │   │   │           │   │   │   ├── PoTokenException.kt
    │   │   │           │   │   │   ├── PoTokenGenerator.kt
    │   │   │           │   │   │   ├── PoTokenResult.kt
    │   │   │           │   │   │   └── PoTokenWebView.kt
    │   │   │           │   │   ├── ArtistNameAliases.kt
    │   │   │           │   │   ├── CoilBitmapLoader.kt
    │   │   │           │   │   ├── ComposeToImage.kt
    │   │   │           │   │   ├── CrashHandler.kt
    │   │   │           │   │   ├── DataStore.kt
    │   │   │           │   │   ├── IconUtils.kt
    │   │   │           │   │   ├── InnerTubeXPlayer.kt
    │   │   │           │   │   ├── NetworkConnectivityObserver.kt
    │   │   │           │   │   ├── PlaylistExporter.kt
    │   │   │           │   │   ├── ScrobbleManager.kt
    │   │   │           │   │   ├── SearchRoutes.kt
    │   │   │           │   │   ├── StringUtils.kt
    │   │   │           │   │   ├── SyncUtils.kt
    │   │   │           │   │   ├── Updater.kt
    │   │   │           │   │   └── Utils.kt
    │   │   │           │   ├── viewmodels/
    │   │   │           │   │   ├── AccountSettingsViewModel.kt
    │   │   │           │   │   ├── AccountViewModel.kt
    │   │   │           │   │   ├── AlbumViewModel.kt
    │   │   │           │   │   ├── ArtistAlbumsViewModel.kt
    │   │   │           │   │   ├── ArtistItemsViewModel.kt
    │   │   │           │   │   ├── ArtistViewModel.kt
    │   │   │           │   │   ├── AutoPlaylistViewModel.kt
    │   │   │           │   │   ├── BackupRestoreViewModel.kt
    │   │   │           │   │   ├── BrowseViewModel.kt
    │   │   │           │   │   ├── CachePlaylistViewModel.kt
    │   │   │           │   │   ├── ChartsViewModel.kt
    │   │   │           │   │   ├── HistoryViewModel.kt
    │   │   │           │   │   ├── HomeViewModel.kt
    │   │   │           │   │   ├── LibraryViewModels.kt
    │   │   │           │   │   ├── LocalPlaylistViewModel.kt
    │   │   │           │   │   ├── LocalSearchViewModel.kt
    │   │   │           │   │   ├── LyricsMenuViewModel.kt
    │   │   │           │   │   ├── LyricsViewModel.kt
    │   │   │           │   │   ├── MoodAndGenresViewModel.kt
    │   │   │           │   │   ├── NewReleaseViewModel.kt
    │   │   │           │   │   ├── OnlinePlaylistViewModel.kt
    │   │   │           │   │   ├── OnlinePodcastViewModel.kt
    │   │   │           │   │   ├── OnlineSearchSuggestionViewModel.kt
    │   │   │           │   │   ├── OnlineSearchViewModel.kt
    │   │   │           │   │   ├── PlaylistsViewModel.kt
    │   │   │           │   │   ├── StatsViewModel.kt
    │   │   │           │   │   ├── TopPlaylistViewModel.kt
    │   │   │           │   │   └── YouTubeBrowseViewModel.kt
    │   │   │           │   ├── widget/
    │   │   │           │   │   ├── MetrolistWidgetManager.kt
    │   │   │           │   │   ├── MusicWidgetReceiver.kt
    │   │   │           │   │   └── TurntableWidgetReceiver.kt
    │   │   │           │   ├── App.kt
    │   │   │           │   └── MainActivity.kt
    │   │   │           ├── paxsenix/
    │   │   │           │   ├── models/
    │   │   │           │   │   └── PaxsenixModels.kt
    │   │   │           │   └── Paxsenix.kt
    │   │   │           ├── shazamkit/
    │   │   │           │   ├── models/
    │   │   │           │   │   └── ShazamModels.kt
    │   │   │           │   └── Shazam.kt
    │   │   │           └── music.zip
    │   │   ├── proto/
    │   │   │   └── listentogether.proto
    │   │   ├── res/
    │   │   │   ├── drawable/
    │   │   │   │   ├── account.xml
    │   │   │   │   ├── add.xml
    │   │   │   │   ├── add_circle.xml
    │   │   │   │   ├── album.xml
    │   │   │   │   ├── app_logo.xml
    │   │   │   │   ├── arrow_back.xml
    │   │   │   │   ├── arrow_downward.xml
    │   │   │   │   ├── arrow_forward.xml
    │   │   │   │   ├── arrow_top_left.xml
    │   │   │   │   ├── arrow_upward.xml
    │   │   │   │   ├── artist.xml
    │   │   │   │   ├── backup.xml
    │   │   │   │   ├── baseline_event_repeat_24.xml
    │   │   │   │   ├── bedtime.xml
    │   │   │   │   ├── bluetooth.xml
    │   │   │   │   ├── bug_report.xml
    │   │   │   │   ├── buymeacoffee.xml
    │   │   │   │   ├── cached.xml
    │   │   │   │   ├── cast.xml
    │   │   │   │   ├── cast_connected.xml
    │   │   │   │   ├── check.xml
    │   │   │   │   ├── clear_all.xml
    │   │   │   │   ├── close.xml
    │   │   │   │   ├── cloud.xml
    │   │   │   │   ├── content_copy.xml
    │   │   │   │   ├── contrast.xml
    │   │   │   │   ├── crop.xml
    │   │   │   │   ├── crown.xml
    │   │   │   │   ├── delete.xml
    │   │   │   │   ├── delete_history.xml
    │   │   │   │   ├── discord.xml
    │   │   │   │   ├── discover_tune.xml
    │   │   │   │   ├── done.xml
    │   │   │   │   ├── download.xml
    │   │   │   │   ├── drag_handle.xml
    │   │   │   │   ├── edit.xml
    │   │   │   │   ├── equalizer.xml
    │   │   │   │   ├── error.xml
    │   │   │   │   ├── expand_less.xml
    │   │   │   │   ├── expand_more.xml
    │   │   │   │   ├── explicit.xml
    │   │   │   │   ├── explore_outlined.xml
    │   │   │   │   ├── fast_forward.xml
    │   │   │   │   ├── favorite.xml
    │   │   │   │   ├── favorite_border.xml
    │   │   │   │   ├── fullscreen.xml
    │   │   │   │   ├── github.xml
    │   │   │   │   ├── gradient.xml
    │   │   │   │   ├── graphic_eq.xml
    │   │   │   │   ├── grid_view.xml
    │   │   │   │   ├── group.xml
    │   │   │   │   ├── group_add.xml
    │   │   │   │   ├── group_filled.xml
    │   │   │   │   ├── group_outlined.xml
    │   │   │   │   ├── hide_image.xml
    │   │   │   │   ├── history.xml
    │   │   │   │   ├── home_filled.xml
    │   │   │   │   ├── home_outlined.xml
    │   │   │   │   ├── ic_android_auto.xml
    │   │   │   │   ├── ic_heart.xml
    │   │   │   │   ├── ic_heart_outline.xml
    │   │   │   │   ├── ic_launcher_background_v31.xml
    │   │   │   │   ├── ic_launcher_foreground.xml
    │   │   │   │   ├── ic_launcher_foreground_v31.xml
    │   │   │   │   ├── ic_launcher_monochrome.xml
    │   │   │   │   ├── ic_launcher_static_foreground.png
    │   │   │   │   ├── ic_push_pin.xml
    │   │   │   │   ├── ic_widget_heart_nav.xml
    │   │   │   │   ├── ic_widget_heart_outline_nav.xml
    │   │   │   │   ├── ic_widget_mic.xml
    │   │   │   │   ├── ic_widget_pause.xml
    │   │   │   │   ├── ic_widget_pause_low.xml
    │   │   │   │   ├── ic_widget_pause_secondary.xml
    │   │   │   │   ├── ic_widget_play.xml
    │   │   │   │   ├── ic_widget_play_low.xml
    │   │   │   │   ├── ic_widget_play_secondary.xml
    │   │   │   │   ├── ic_widget_skip_next.xml
    │   │   │   │   ├── ic_widget_skip_previous.xml
    │   │   │   │   ├── info.xml
    │   │   │   │   ├── insert_photo.xml
    │   │   │   │   ├── instagram.xml
    │   │   │   │   ├── integration.xml
    │   │   │   │   ├── key.xml
    │   │   │   │   ├── key_vertical.xml
    │   │   │   │   ├── language.xml
    │   │   │   │   ├── language_korean_latin.xml
    │   │   │   │   ├── library_add.xml
    │   │   │   │   ├── library_add_check.xml
    │   │   │   │   ├── library_music.xml
    │   │   │   │   ├── library_music_filled.xml
    │   │   │   │   ├── library_music_outlined.xml
    │   │   │   │   ├── linear_scale.xml
    │   │   │   │   ├── link.xml
    │   │   │   │   ├── list.xml
    │   │   │   │   ├── location_on.xml
    │   │   │   │   ├── lock.xml
    │   │   │   │   ├── lock_open.xml
    │   │   │   │   ├── login.xml
    │   │   │   │   ├── logout.xml
    │   │   │   │   ├── lyrics.xml
    │   │   │   │   ├── manage_search.xml
    │   │   │   │   ├── media3_icon_bookmark_filled.xml
    │   │   │   │   ├── media3_icon_feed.xml
    │   │   │   │   ├── media3_icon_thumb_down_unfilled.xml
    │   │   │   │   ├── media3_icon_thumb_up_unfilled.xml
    │   │   │   │   ├── mic.xml
    │   │   │   │   ├── more_horiz.xml
    │   │   │   │   ├── more_time.xml
    │   │   │   │   ├── more_vert.xml
    │   │   │   │   ├── music_note.xml
    │   │   │   │   ├── nav_bar.xml
    │   │   │   │   ├── navigate_next.xml
    │   │   │   │   ├── newspaper.xml
    │   │   │   │   ├── notification.xml
    │   │   │   │   ├── offline.xml
    │   │   │   │   ├── palette.xml
    │   │   │   │   ├── pause.xml
    │   │   │   │   ├── person.xml
    │   │   │   │   ├── play.xml
    │   │   │   │   ├── playlist_add.xml
    │   │   │   │   ├── playlist_play.xml
    │   │   │   │   ├── queue_music.xml
    │   │   │   │   ├── radio.xml
    │   │   │   │   ├── radio_button_checked.xml
    │   │   │   │   ├── radio_button_unchecked.xml
    │   │   │   │   ├── refresh.xml
    │   │   │   │   ├── remove.xml
    │   │   │   │   ├── repeat.xml
    │   │   │   │   ├── repeat_on.xml
    │   │   │   │   ├── repeat_one.xml
    │   │   │   │   ├── repeat_one_on.xml
    │   │   │   │   ├── replay.xml
    │   │   │   │   ├── restore.xml
    │   │   │   │   ├── screenshot.xml
    │   │   │   │   ├── search.xml
    │   │   │   │   ├── search_off.xml
    │   │   │   │   ├── security.xml
    │   │   │   │   ├── select_all.xml
    │   │   │   │   ├── settings.xml
    │   │   │   │   ├── share.xml
    │   │   │   │   ├── shortcut_library.xml
    │   │   │   │   ├── shortcut_search.xml
    │   │   │   │   ├── shuffle.xml
    │   │   │   │   ├── shuffle_on.xml
    │   │   │   │   ├── similar.xml
    │   │   │   │   ├── skip_next.xml
    │   │   │   │   ├── skip_previous.xml
    │   │   │   │   ├── sliders.xml
    │   │   │   │   ├── slow_motion_video.xml
    │   │   │   │   ├── small_icon.xml
    │   │   │   │   ├── speed.xml
    │   │   │   │   ├── stats.xml
    │   │   │   │   ├── storage.xml
    │   │   │   │   ├── subscribe.xml
    │   │   │   │   ├── subscribed.xml
    │   │   │   │   ├── swipe.xml
    │   │   │   │   ├── sync.xml
    │   │   │   │   ├── tab.xml
    │   │   │   │   ├── telegram.xml
    │   │   │   │   ├── time_auto.xml
    │   │   │   │   ├── timer.xml
    │   │   │   │   ├── timer_arrow_down.xml
    │   │   │   │   ├── token.xml
    │   │   │   │   ├── translate.xml
    │   │   │   │   ├── trending_up.xml
    │   │   │   │   ├── tune.xml
    │   │   │   │   ├── update.xml
    │   │   │   │   ├── upload.xml
    │   │   │   │   ├── volume_down.xml
    │   │   │   │   ├── volume_mute.xml
    │   │   │   │   ├── volume_off.xml
    │   │   │   │   ├── volume_off_pause.xml
    │   │   │   │   ├── volume_up.xml
    │   │   │   │   ├── warning.xml
    │   │   │   │   ├── widget_background.xml
    │   │   │   │   ├── widget_like_button_bg.xml
    │   │   │   │   ├── widget_mic_button_bg.xml
    │   │   │   │   ├── widget_mic_button_bg_active.xml
    │   │   │   │   ├── widget_mic_pulse_1.xml
    │   │   │   │   ├── widget_mic_pulse_2.xml
    │   │   │   │   ├── widget_mic_pulse_3.xml
    │   │   │   │   ├── widget_mic_pulse_4.xml
    │   │   │   │   ├── widget_mic_pulse_idle.xml
    │   │   │   │   ├── widget_play_button_circular.xml
    │   │   │   │   ├── widget_play_pill_bg.xml
    │   │   │   │   ├── widget_progress_clip.xml
    │   │   │   │   ├── widget_progress_fill.xml
    │   │   │   │   ├── widget_progress_track.xml
    │   │   │   │   ├── widget_turntable_default_art.xml
    │   │   │   │   ├── widget_turntable_nav_bg.xml
    │   │   │   │   ├── widget_turntable_play_bg.xml
    │   │   │   │   ├── wifi_proxy.xml
    │   │   │   │   ├── wrapped_playlistv1.png
    │   │   │   │   └── wrapped_playlistv2.png
    │   │   │   ├── drawable-night/
    │   │   │   │   ├── widget_background.xml
    │   │   │   │   ├── widget_play_pill_bg.xml
    │   │   │   │   ├── widget_progress_fill.xml
    │   │   │   │   ├── widget_progress_track.xml
    │   │   │   │   ├── widget_turntable_nav_bg.xml
    │   │   │   │   └── widget_turntable_play_bg.xml
    │   │   │   ├── drawable-night-v31/
    │   │   │   │   ├── widget_background.xml
    │   │   │   │   ├── widget_play_pill_bg.xml
    │   │   │   │   ├── widget_progress_fill.xml
    │   │   │   │   ├── widget_progress_track.xml
    │   │   │   │   ├── widget_turntable_nav_bg.xml
    │   │   │   │   └── widget_turntable_play_bg.xml
    │   │   │   ├── drawable-v31/
    │   │   │   │   ├── ic_launcher_background_v31.xml
    │   │   │   │   ├── ic_widget_mic.xml
    │   │   │   │   ├── widget_background.xml
    │   │   │   │   ├── widget_mic_button_bg.xml
    │   │   │   │   ├── widget_mic_button_bg_active.xml
    │   │   │   │   ├── widget_mic_pulse_1.xml
    │   │   │   │   ├── widget_mic_pulse_2.xml
    │   │   │   │   ├── widget_mic_pulse_3.xml
    │   │   │   │   ├── widget_mic_pulse_4.xml
    │   │   │   │   ├── widget_play_pill_bg.xml
    │   │   │   │   ├── widget_progress_fill.xml
    │   │   │   │   ├── widget_progress_track.xml
    │   │   │   │   ├── widget_turntable_nav_bg.xml
    │   │   │   │   └── widget_turntable_play_bg.xml
    │   │   │   ├── font/
    │   │   │   │   ├── bbh_bartle.xml
    │   │   │   │   ├── bbh_bartle_regular.ttf
    │   │   │   │   ├── doto.ttf
    │   │   │   │   └── inter.ttf
    │   │   │   ├── layout/
    │   │   │   │   ├── widget_compact_square.xml
    │   │   │   │   ├── widget_compact_wide.xml
    │   │   │   │   ├── widget_music_player.xml
    │   │   │   │   ├── widget_playlist_preview.xml
    │   │   │   │   └── widget_turntable.xml
    │   │   │   ├── mipmap-anydpi-v26/
    │   │   │   │   ├── ic_launcher.xml
    │   │   │   │   ├── ic_launcher_round.xml
    │   │   │   │   ├── ic_launcher_static.xml
    │   │   │   │   └── ic_launcher_static_round.xml
    │   │   │   ├── mipmap-anydpi-v31/
    │   │   │   │   ├── ic_launcher.xml
    │   │   │   │   └── ic_launcher_round.xml
    │   │   │   ├── mipmap-hdpi/
    │   │   │   │   ├── ic_launcher.webp
    │   │   │   │   ├── ic_launcher_round.webp
    │   │   │   │   └── ic_launcher_static.png
    │   │   │   ├── mipmap-mdpi/
    │   │   │   │   ├── ic_launcher.webp
    │   │   │   │   ├── ic_launcher_round.webp
    │   │   │   │   └── ic_launcher_static.png
    │   │   │   ├── mipmap-xhdpi/
    │   │   │   │   ├── ic_launcher.webp
    │   │   │   │   ├── ic_launcher_round.webp
    │   │   │   │   ├── ic_launcher_static.png
    │   │   │   │   └── tv_banner.png
    │   │   │   ├── mipmap-xxhdpi/
    │   │   │   │   ├── ic_launcher.webp
    │   │   │   │   ├── ic_launcher_round.webp
    │   │   │   │   └── ic_launcher_static.png
    │   │   │   ├── mipmap-xxxhdpi/
    │   │   │   │   ├── ic_launcher.webp
    │   │   │   │   ├── ic_launcher_round.webp
    │   │   │   │   └── ic_launcher_static.png
    │   │   │   ├── raw/
    │   │   │   │   └── wrapped_theme.mp3
    │   │   │   ├── values/
    │   │   │   │   ├── app_name.xml
    │   │   │   │   ├── colors.xml
    │   │   │   │   ├── ic_launcher_background.xml
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   ├── orbit_colors.xml
    │   │   │   │   ├── strings.xml
    │   │   │   │   ├── styles.xml
    │   │   │   │   ├── values.xml
    │   │   │   │   └── widget_colors.xml
    │   │   │   ├── values-af/
    │   │   │   │   └── metrolist_strings.xml
    │   │   │   ├── values-ar/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-az/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-b+sr+Latn/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-be/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-bg/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-bn/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-bn-rIN/
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-bs/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-ca/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-cs/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-de/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-el/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-en-rCA/
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-es/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-es-rUS/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-et/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-eu/
    │   │   │   │   └── metrolist_strings.xml
    │   │   │   ├── values-fa/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-fi/
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-fil/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-fr/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-gl/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-hi/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-hr/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-hu/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-in/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-it/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-iw/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-ja/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-ko/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-lt/
    │   │   │   │   └── metrolist_strings.xml
    │   │   │   ├── values-ml/
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-ms/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-nb-rNO/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-night/
    │   │   │   │   ├── colors.xml
    │   │   │   │   ├── orbit_colors.xml
    │   │   │   │   └── widget_colors.xml
    │   │   │   ├── values-night-v31/
    │   │   │   │   └── widget_colors.xml
    │   │   │   ├── values-nl/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-nn/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-pa/
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-pl/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-pt/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-pt-rBR/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-ro/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-ru/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-sk/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-sl/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-sq/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-ta/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-te/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-th/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-tr/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-uk/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-v31/
    │   │   │   │   ├── styles.xml
    │   │   │   │   └── widget_colors.xml
    │   │   │   ├── values-vi/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-zh-rCN/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── values-zh-rTW/
    │   │   │   │   ├── metrolist_strings.xml
    │   │   │   │   └── strings.xml
    │   │   │   ├── xml/
    │   │   │   │   ├── automotive_app_desc.xml
    │   │   │   │   ├── backup_rules.xml
    │   │   │   │   ├── data_extraction_rules.xml
    │   │   │   │   ├── music_widget_info.xml
    │   │   │   │   ├── network_security_config.xml
    │   │   │   │   ├── provider_paths.xml
    │   │   │   │   └── shortcuts.xml
    │   │   │   ├── xml-v31/
    │   │   │   │   ├── music_widget_info.xml
    │   │   │   │   └── turntable_widget_info.xml
    │   │   │   └── resources.properties
    │   │   ├── AndroidManifest.xml
    │   │   └── ic_launcher-playstore.png
    │   └── test/
    │       └── kotlin/
    │           └── com/
    │               └── metrolist/
    │                   └── music/
    │                       ├── api/
    │                       │   └── OpenRouterServiceTest.kt
    │                       ├── betterlyrics/
    │                       │   └── TTMLParserTest.kt
    │                       ├── db/
    │                       │   ├── entities/
    │                       │   │   ├── LocalIdTest.kt
    │                       │   │   └── SongEntityTest.kt
    │                       │   ├── AddSongsToPlaylistTest.kt
    │                       │   ├── AndroidAutoDatabasePaginationTest.kt
    │                       │   ├── ArtistPageCacheTest.kt
    │                       │   ├── HistoryDatabaseDeduplicationTest.kt
    │                       │   ├── MusicDatabaseMigrationTest.kt
    │                       │   ├── PlaylistDuplicatesBatchedTest.kt
    │                       │   └── SongMetadataEditingTest.kt
    │                       ├── discord/
    │                       │   ├── DiscordAuthTest.kt
    │                       │   ├── DiscordPresenceTest.kt
    │                       │   ├── DiscordReconnectStrategyTest.kt
    │                       │   └── DiscordTokenStoreTest.kt
    │                       ├── extensions/
    │                       │   ├── ContentResolverExtTest.kt
    │                       │   └── MediaItemExtTest.kt
    │                       ├── listentogether/
    │                       │   ├── MessageCodecTest.kt
    │                       │   ├── PlaybackSyncTest.kt
    │                       │   ├── QueueSyncTest.kt
    │                       │   └── ServerClockTest.kt
    │                       ├── lyrics/
    │                       │   ├── LyricsBackgroundTimingTest.kt
    │                       │   └── MacedonianRomanizationTest.kt
    │                       ├── playback/
    │                       │   ├── audio/
    │                       │   │   └── AudioProcessorBufferReuseTest.kt
    │                       │   ├── DownloadContentLengthTest.kt
    │                       │   ├── DownloadUtilTest.kt
    │                       │   ├── MediaLibrarySessionCallbackTest.kt
    │                       │   ├── NormalizationGainTest.kt
    │                       │   ├── StreamUrlCacheTest.kt
    │                       │   └── VoiceSearchMatcherTest.kt
    │                       ├── ui/
    │                       │   ├── player/
    │                       │   │   └── PlaybackErrorTest.kt
    │                       │   └── utils/
    │                       │       └── YouTubeUtilsTest.kt
    │                       ├── utils/
    │                       │   ├── ArtistNameAliasesTest.kt
    │                       │   ├── ComposeToImageTest.kt
    │                       │   ├── InnerTubeXPlayerTest.kt
    │                       │   ├── NetworkConnectivityObserverTest.kt
    │                       │   ├── PlaylistSyncTest.kt
    │                       │   └── UpdaterTest.kt
    │                       └── viewmodels/
    │                           ├── CachePlaylistPartitionTest.kt
    │                           └── HomeSpeedDialTest.kt
    ├── .gitignore
    ├── build.gradle.kts
    ├── lint.xml
    └── proguard-rules.pro
```

## assets

Static assets (badges).

```
assets/
    ├── badges/
    │   ├── github-nightly.svg
    │   ├── github-stable.svg
    │   ├── izzyondroid.svg
    │   ├── obtainium.svg
    │   └── openapk.svg
    ├── XMR.png
    ├── blacksmith-powered.png
    ├── buymeacoffee.png
    └── ic_launcher-playstore.png
```

## fastlane

Fastlane metadata/store listings.

```
fastlane/
```

## gradle

Gradle wrapper.

```
gradle/
    ├── wrapper/
    │   ├── gradle-wrapper.jar
    │   └── gradle-wrapper.properties
    ├── gradle-daemon-jvm.properties
    └── libs.versions.toml
```

## innertube

InnerTube API client module.

```
innertube/
    ├── src/
    │   ├── main/
    │   │   ├── kotlin/
    │   │   │   └── com/
    │   │   │       └── metrolist/
    │   │   │           └── innertube/
    │   │   │               ├── models/
    │   │   │               │   ├── response/
    │   │   │               │   │   ├── AccountMenuResponse.kt
    │   │   │               │   │   ├── BrowseResponse.kt
    │   │   │               │   │   ├── ContinuationResponse.kt
    │   │   │               │   │   ├── CreatePlaylistResponse.kt
    │   │   │               │   │   ├── EditPlaylistResponse.kt
    │   │   │               │   │   ├── FeedbackResponse.kt
    │   │   │               │   │   ├── GetQueueResponse.kt
    │   │   │               │   │   ├── GetSearchSuggestionsResponse.kt
    │   │   │               │   │   ├── GetTranscriptResponse.kt
    │   │   │               │   │   ├── NextResponse.kt
    │   │   │               │   │   ├── PlayerResponse.kt
    │   │   │               │   │   └── SearchResponse.kt
    │   │   │               │   ├── AccountInfo.kt
    │   │   │               │   ├── AutomixPreviewVideoRenderer.kt
    │   │   │               │   ├── Badges.kt
    │   │   │               │   ├── Button.kt
    │   │   │               │   ├── Continuation.kt
    │   │   │               │   ├── ContinuationItemRenderer.kt
    │   │   │               │   ├── Endpoint.kt
    │   │   │               │   ├── GridRenderer.kt
    │   │   │               │   ├── Icon.kt
    │   │   │               │   ├── MediaInfo.kt
    │   │   │               │   ├── Menu.kt
    │   │   │               │   ├── MusicCardShelfRenderer.kt
    │   │   │               │   ├── MusicCarouselShelfRenderer.kt
    │   │   │               │   ├── MusicDescriptionShelfRenderer.kt
    │   │   │               │   ├── MusicEditablePlaylistDetailHeaderRenderer.kt
    │   │   │               │   ├── MusicMultiRowListItemRenderer.kt
    │   │   │               │   ├── MusicNavigationButtonRenderer.kt
    │   │   │               │   ├── MusicPlaylistShelfRenderer.kt
    │   │   │               │   ├── MusicQueueRenderer.kt
    │   │   │               │   ├── MusicResponsiveHeaderRenderer.kt
    │   │   │               │   ├── MusicResponsiveListItemRenderer.kt
    │   │   │               │   ├── MusicShelfRenderer.kt
    │   │   │               │   ├── MusicTwoRowItemRenderer.kt
    │   │   │               │   ├── NavigationEndpoint.kt
    │   │   │               │   ├── PlaylistPanelRenderer.kt
    │   │   │               │   ├── PlaylistPanelVideoRenderer.kt
    │   │   │               │   ├── ResponseContext.kt
    │   │   │               │   ├── ReturnYouTubeDislikeResponse.kt
    │   │   │               │   ├── Runs.kt
    │   │   │               │   ├── SearchSuggestions.kt
    │   │   │               │   ├── SearchSuggestionsSectionRenderer.kt
    │   │   │               │   ├── SectionListRenderer.kt
    │   │   │               │   ├── SubscriptionButton.kt
    │   │   │               │   ├── Tabs.kt
    │   │   │               │   ├── ThumbnailRenderer.kt
    │   │   │               │   ├── Thumbnails.kt
    │   │   │               │   ├── TwoColumnBrowseResultsRenderer.kt
    │   │   │               │   ├── UrlEndpoint.kt
    │   │   │               │   ├── YTItem.kt
    │   │   │               │   ├── YouTubeAccount.kt
    │   │   │               │   ├── YouTubeClient.kt
    │   │   │               │   ├── YouTubeDataPage.kt
    │   │   │               │   └── YouTubeLocale.kt
    │   │   │               ├── pages/
    │   │   │               │   ├── AlbumPage.kt
    │   │   │               │   ├── ArtistItemsContinuationPage.kt
    │   │   │               │   ├── ArtistItemsPage.kt
    │   │   │               │   ├── ArtistPage.kt
    │   │   │               │   ├── BrowseResult.kt
    │   │   │               │   ├── ChartsPage.kt
    │   │   │               │   ├── ExplorePage.kt
    │   │   │               │   ├── HistoryPage.kt
    │   │   │               │   ├── HomePage.kt
    │   │   │               │   ├── LibraryContinuationPage.kt
    │   │   │               │   ├── LibraryPage.kt
    │   │   │               │   ├── MoodAndGenres.kt
    │   │   │               │   ├── NewReleaseAlbumPage.kt
    │   │   │               │   ├── NextPage.kt
    │   │   │               │   ├── PageHelper.kt
    │   │   │               │   ├── PlaylistContinuationPage.kt
    │   │   │               │   ├── PlaylistPage.kt
    │   │   │               │   ├── PodcastPage.kt
    │   │   │               │   ├── RelatedPage.kt
    │   │   │               │   ├── SearchPage.kt
    │   │   │               │   ├── SearchSuggestionPage.kt
    │   │   │               │   └── SearchSummaryPage.kt
    │   │   │               ├── utils/
    │   │   │               │   ├── Utils.kt
    │   │   │               │   └── YouTubeUrlParser.kt
    │   │   │               ├── InnerTube.kt
    │   │   │               ├── YouTube.kt
    │   │   │               └── YouTubeConstants.kt
    │   │   └── AndroidManifest.xml
    │   └──test /
    │       └── kotlin/
    │           └── com/
    │               └── metrolist/
    │                   └── innertube/
    │                       ├── models/
    │                       │   └── MusicResponsiveHeaderRendererTest.kt
    │                       ├── pages/
    │                       │   ├── AlbumPageTest.kt
    │                       │   ├── PageHelperTest.kt
    │                       │   ├── PlaylistPageTest.kt
    │                       │   ├── PodcastPageTest.kt
    │                       │   └── SearchPageTest.kt
    │                       ├── SearchAuthenticationTest.kt
    │                       └── UploadProgressInputStreamTest.kt
    ├── .gitignore
    └── build.gradle.kts
```

## tests

Standalone test harness/scripts.

tests
    ├── Error73.txt
    ├── Summary of Mentsic(cline)
    └── TempNotes.md
```
