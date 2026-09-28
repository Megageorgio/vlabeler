package com.sdercolin.vlabeler.video

import android.media.MediaPlayer
import android.os.Build
import android.view.Surface
import com.sdercolin.vlabeler.env.Log

class MediaPlayerComponent(val mediaPlayer: MediaPlayer)

/**
 * Video player. Android: a wrapper of android.media.MediaPlayer (instead of vlcj).
 */
class VideoPlayer {
    var mediaPlayerComponent: MediaPlayerComponent? = null
        private set
    private var mediaPlayer: MediaPlayer? = null
    private var prepared = false
    private var pendingSeek: Long? = null
    private var shouldPlay = false

    var surface: Surface? = null
        set(value) {
            field = value
            runCatching { mediaPlayer?.setSurface(value) }.onFailure { Log.error(it) }
        }

    val currentTime: Long?
        get() = mediaPlayer?.takeIf { prepared }?.let { runCatching { it.currentPosition.toLong() }.getOrNull() }

    fun init() = runCatching {
        Log.info("VideoPlayer init")
        val player = MediaPlayer()
        mediaPlayer = player
        mediaPlayerComponent = MediaPlayerComponent(player)
        surface?.let { player.setSurface(it) }
    }.onFailure {
        mediaPlayerComponent = null
        mediaPlayer = null
        Log.error(it)
    }

    fun load(url: String): VideoPlayer {
        mediaPlayer?.let {
            runCatching {
                prepared = false
                it.reset()
                it.setDataSource(url)
                surface?.let { surface -> it.setSurface(surface) }
                it.prepare()
                prepared = true
                pendingSeek?.let { time -> seek(it, time) }
                pendingSeek = null
                if (shouldPlay) it.start()
                Log.info("VideoPlayer loaded file \"$url\"")
            }.onFailure { e -> Log.error(e) }
        }
        return this
    }

    private fun seek(player: MediaPlayer, time: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            player.seekTo(time, MediaPlayer.SEEK_CLOSEST)
        } else {
            player.seekTo(time.toInt())
        }
    }

    fun startAt(time: Long): VideoPlayer {
        mediaPlayer?.let {
            if (prepared) {
                runCatching { seek(it, time) }.onFailure { e -> Log.error(e) }
            } else {
                pendingSeek = time
            }
            Log.info("VideoPlayer play at ${time}ms")
        }
        return this
    }

    fun pause(): VideoPlayer {
        shouldPlay = false
        mediaPlayer?.takeIf { prepared }?.let { runCatching { if (it.isPlaying) it.pause() } }
        return this
    }

    fun play(): VideoPlayer {
        shouldPlay = true
        mediaPlayer?.takeIf { prepared }?.let { runCatching { it.start() } }
        return this
    }

    fun mute(): VideoPlayer {
        mediaPlayer?.let { runCatching { it.setVolume(0f, 0f) } }
        return this
    }

    fun release() {
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        mediaPlayerComponent = null
        prepared = false
    }
}
