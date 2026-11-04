/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.annotation.DrawableRes
import com.metrolist.music.R

/**
 * Centralized, typed icon registry for the Mentsic design system.
 * Guarantees standard visual sizing, semantic alignment, and zero runtime bloat.
 */
object MentsicIcons {
    // Playback controls
    @DrawableRes val Play = R.drawable.play
    @DrawableRes val Pause = R.drawable.pause
    @DrawableRes val SkipNext = R.drawable.skip_next
    @DrawableRes val SkipPrevious = R.drawable.skip_previous
    @DrawableRes val Shuffle = R.drawable.shuffle
    @DrawableRes val ShuffleOn = R.drawable.shuffle_on
    @DrawableRes val Repeat = R.drawable.repeat
    @DrawableRes val RepeatOn = R.drawable.repeat_on
    @DrawableRes val RepeatOneOn = R.drawable.repeat_one_on

    // Favorites & Actions
    @DrawableRes val Favorite = R.drawable.favorite
    @DrawableRes val FavoriteBorder = R.drawable.favorite_border
    @DrawableRes val Queue = R.drawable.queue_music
    @DrawableRes val Lyrics = R.drawable.lyrics
    @DrawableRes val Equalizer = R.drawable.equalizer
    @DrawableRes val Cast = R.drawable.cast
    @DrawableRes val CastConnected = R.drawable.cast_connected

    // Navigation & Primary Hubs
    @DrawableRes val HomeOutlined = R.drawable.home_outlined
    @DrawableRes val HomeFilled = R.drawable.home_filled
    @DrawableRes val Search = R.drawable.search
    @DrawableRes val LibraryOutlined = R.drawable.library_music_outlined
    @DrawableRes val LibraryFilled = R.drawable.library_music_filled
    @DrawableRes val TogetherOutlined = R.drawable.group_outlined
    @DrawableRes val TogetherFilled = R.drawable.group_filled

    // Chrome & Utilities
    @DrawableRes val History = R.drawable.history
    @DrawableRes val Stats = R.drawable.stats
    @DrawableRes val Settings = R.drawable.settings
    @DrawableRes val Account = R.drawable.account
    @DrawableRes val Close = R.drawable.close
    @DrawableRes val ArrowBack = R.drawable.arrow_back
    @DrawableRes val MoreVert = R.drawable.more_vert
    @DrawableRes val MoreHoriz = R.drawable.more_horiz
    @DrawableRes val SleepTimer = R.drawable.bedtime
}

