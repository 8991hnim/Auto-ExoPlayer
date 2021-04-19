package com.gg.gapo.video.hnim_exo

import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ui.PlayerView
import m.tech.demoexopool.hnim_exo.ExoProvider3
import m.tech.demoexopool.hnim_exo.HnimExoController
import java.lang.ref.WeakReference

/**
 * @author: 89hnim
 * @since: 12/04/2021
 */
class ExoController(
    private val context: Context
) : HnimExoController {

    private var autoPlay: Boolean = false
    private var isMuted: Boolean = false
    private var isSaveState: Boolean = false
    private var isAutoMoveNext: Boolean = false

    private val exoProvider3: ExoProvider3 by lazy {
        ExoProvider3(context)
    }

    //hàm thật sự play video
    fun togglePlayer(currentPosition: Int, playerView: PlayerView?) {
        Log.d(TAG, "togglePlayer: $currentPosition")
        if (playerView == null) return

        exoProvider3.setupWith(currentPosition, playerView)
    }

    //gọi khi lifecycle onPause
    fun pauseAll() {
        exoProvider3.exoPlayer().playWhenReady = false
    }

    override fun setupWith(
        position: Int,
        source: String,
        preloadSource: Array<String?>?,
        preloadImageSource: Array<String?>?,
        thumbSource: String?,
        thumbnail: AppCompatImageView?,
        loadingView: View?,
        listener: HnimExoPlayerListener
    ) {
        exoProvider3.setupWith(
            position = position,
            source = source,
            preloadSource = preloadSource,
            preloadImageSource = preloadImageSource,
            thumbSource = thumbSource,
            thumbnail = if (thumbnail != null) WeakReference(thumbnail) else null,
            loadingView = if (loadingView != null) WeakReference(loadingView) else null,
            isMoveToNext = isAutoMoveNext,
            listener = listener
        )
    }

    override fun play(position: Int) {
        exoProvider3.exoPlayer().playWhenReady = true
    }

    override fun pause(position: Int) {
        exoProvider3.exoPlayer().playWhenReady = false
    }

    override fun stop(position: Int) {
        exoProvider3.exoPlayer().stop()
    }

    override fun seekTo(position: Int, positionMs: Long) {
        exoProvider3.exoPlayer().seekTo(positionMs)
    }

    override fun setAutoPlay(autoPlay: Boolean) {
        this.autoPlay = autoPlay
    }

    override fun setMuted(position: Int, isMuted: Boolean) {
        this.isMuted = isMuted
        exoProvider3.exoPlayer().volume = if (isMuted) 0f else 1f
    }

    override fun setSaveState(saveState: Boolean) {
        this.isSaveState = saveState
    }

    fun setAutoMoveNext(isAutoMoveNext: Boolean) {
        this.isAutoMoveNext = isAutoMoveNext
    }

    fun clear(isDestroy: Boolean) {
        exoProvider3.exoPlayer().release()
        exoProvider3.setCurrentPosition(0)
    }

    interface HnimExoPlayerListener {
        fun onBuffering() {}

        fun onReady() {}

        fun onEnded() {}

        fun onPlayingChanged(isPlaying: Boolean) {}

        fun onError(exception: ExoPlaybackException) {}
    }

    companion object {
        private const val TAG = "HnimExo::ExoController"
    }
}
