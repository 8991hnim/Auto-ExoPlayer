package m.tech.demoexopool.hnim_exo

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import org.simple.eventbus.EventBus
import java.lang.ref.WeakReference

/**
 * @author: 89hnim
 * @since: 19/04/2021
 * Using single exoPlayer improve performance
 */
class ExoProvider3
constructor(
    private val context: Context
) {

    //Min Video you want to buffer before start Playing it
    private val MIN_PLAYBACK_START_BUFFER = 1000

    private val listVideos = HashMap<Int, String>()

    private val exoPlayer by lazy {
        SimpleExoPlayer.Builder(context)
//            .setMediaSourceFactory(
//                DefaultMediaSourceFactory(SimpleCacheFactory.getCacheDataSource(context))
//            )
//            .setLoadControl(
//                DefaultLoadControl.Builder()
//                    .setAllocator(DefaultAllocator(true, 16))
//                    .setBufferDurationsMs(
//                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
//                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
//                        MIN_PLAYBACK_START_BUFFER,
//                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
//                    ).build()
//            )
            .build()
    }

    private var currentPosition: Int = 0

    fun setupWith(position: Int, playerView: PlayerView) {
        with(exoPlayer) {
            this@ExoProvider3.currentPosition = position
            stop()

            playerView.player = null
            playerView.player = this
            val mediaSource = buildMediaSource(Uri.parse(listVideos[position]))

            playWhenReady = true
            prepare(mediaSource)

            Log.d(TAG, "setupWith: playing $position - ${listVideos[position]}")
        }
    }

    //hàm prepare exo
    fun setupWith(
        position: Int,
        source: String,
        preloadSource: Array<String?>?,
        preloadImageSource: Array<String?>?,
        thumbSource: String?,
        thumbnail: WeakReference<AppCompatImageView>?,
        loadingView: WeakReference<View>?,
        isMoveToNext: Boolean,
        listener: ExoController.HnimExoPlayerListener
    ) {
        Log.d(TAG, "setupWith: setting up $position")

        listVideos[position] = source

        //load thumb
        thumbnail?.get()?.let { Glide.with(it).load(thumbSource).into(it) }

        //callback
        registerPlayerListener(
            position = position,
            thumbnail = thumbnail,
            loadingView = loadingView,
            isMoveToNext = isMoveToNext,
            listener = listener
        )

        //preload
        preloadSource?.let {
            for (s in it) {
                SimpleCacheFactory.preloadVideo(context, s)
            }
        }

        SimpleCacheFactory.preloadImages(context, preloadImageSource)
    }

    private fun registerPlayerListener(
        position: Int,
        thumbnail: WeakReference<AppCompatImageView>?,
        loadingView: WeakReference<View>?,
        isMoveToNext: Boolean,
        listener: ExoController.HnimExoPlayerListener
    ) {
        exoPlayer.addListener(object : EventListener {

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                if (position == currentPosition) {
                    loadingView?.get()?.visibility = View.GONE
                }
                listener.onError(error)
            }

            override fun onSeekProcessed() {
                if (currentPosition == position) {
                    thumbnail?.get()?.isVisible = false
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, state: Int) {
                super.onPlayerStateChanged(playWhenReady, state)
                when (state) {
                    STATE_BUFFERING -> {
                        //nếu buffering ở đầu video thì mới show thumb
                        if (position == currentPosition && exoPlayer.currentPosition <= 1000) {
                            thumbnail?.get()?.visibility = View.VISIBLE
                            loadingView?.get()?.visibility = View.VISIBLE
                        }
                        listener.onBuffering()
                    }
                    STATE_READY -> {
                        if (position == currentPosition) {
                            thumbnail?.get()?.visibility = View.GONE
                            loadingView?.get()?.visibility = View.GONE
                        }
                        listener.onReady()
                    }
                    STATE_ENDED -> {
                        if (isMoveToNext) {
                            EventBus.getDefault().post(0, BusEvent.HE_MOVE_TO_NEXT)
                        }
                        listener.onEnded()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                listener.onPlayingChanged(isPlaying)
                Log.d(TAG, "onIsPlayingChanged: $currentPosition $position")
                if (isPlaying && currentPosition == position)
                    thumbnail?.get()?.visibility = View.GONE
                else if (!isPlaying && currentPosition != position && exoPlayer.currentPosition <= 1000)
                    thumbnail?.get()?.visibility = View.VISIBLE
            }
        })
    }

    fun exoPlayer() = exoPlayer

    fun setCurrentPosition(position: Int) {
        this.currentPosition = position
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val type = Util.inferContentType(uri)
        val dataSourceFactory = SimpleCacheFactory.getCacheDataSource(context)
        return when (type) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
//                C.TYPE_SS -> com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource.Factory(
//                    dataSourceFactory
//                ).createMediaSource(mediaItem)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true) //faster start-up times
                .createMediaSource(uri)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        }
    }

    companion object {
        private const val TAG = "HnimExo::ExoProvider"
    }
}
