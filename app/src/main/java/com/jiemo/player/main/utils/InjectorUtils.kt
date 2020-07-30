package com.jiemo.player.main.utils

import android.app.Application
import android.content.ComponentName
import android.content.Context
import com.example.android.uamp.common.MediaSessionConnection
import com.example.android.uamp.media.MusicService
import com.jiemo.player.main.MainActivityViewModel
import com.jiemo.player.main.items.MediaItemFragmentViewModel
import com.jiemo.player.main.nowplaying.NowPlayingFragmentViewModel

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {
    private fun provideMediaSessionConnection(context: Context): MediaSessionConnection {
        return MediaSessionConnection.getInstance(context,
                ComponentName(context, MusicService::class.java))
    }

    fun provideMainActivityViewModel(context: Context): MainActivityViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return MainActivityViewModel.Factory(mediaSessionConnection)
    }

    fun provideMediaItemFragmentViewModel(context: Context, mediaId: String)
            : MediaItemFragmentViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return MediaItemFragmentViewModel.Factory(mediaId, mediaSessionConnection)
    }

    fun provideNowPlayingFragmentViewModel(context: Context)
            : NowPlayingFragmentViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return NowPlayingFragmentViewModel.Factory(
                applicationContext as Application, mediaSessionConnection)
    }
}