package m.tech.demoexopool.hnim_exo

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.simple.eventbus.EventBus
import java.lang.ref.WeakReference

/**
 * @author: 89hnim
 * @since: 12/04/2021
 * Old class: Not use Pool
 * average time setupWith function: 80ms
 */
class ExoProvider
constructor(
    private var exoPool: Int,
    private val context: WeakReference<Context>
) {

    init {
        Log.d("MTEST", "called:  $exoPool")

    }

    private val glide by lazy { context.get()?.let { Glide.with(it) } }
    private val exoPlayers = HashMap<Int, SimpleExoPlayer>()
    private var maxPosition = 0
    private var minPosition = Integer.MAX_VALUE
    private var currentPosition = 0

    fun clear() {
        exoPlayers.values.forEach {
            it.release()
        }
        maxPosition = 0
        minPosition = Integer.MAX_VALUE
        currentPosition = 0
        exoPlayers.clear()
        Log.d(TAG, "clear: $exoPlayers")
    }

    //hàm prepare exo
    fun setupWith(
        position: Int,
        source: String,
        thumbSource: String?,
        thumbnail: WeakReference<AppCompatImageView>?,
        loadingView: WeakReference<View>?,
        useController: Boolean,
        playerView: WeakReference<PlayerView>,
        isMoveToNext: Boolean,
        listener: ExoController.HnimExoPlayerListener
    ) {
        Log.d(TAG, "setupWith: $position - ${playerView.hashCode()}")

        //test time: average ~80ms setup done.
//        val time = HnimExoUtils.executeTimeInMillis {
        context.get()?.let { context ->
            if (exoPlayers[position] == null) {
                SimpleExoPlayer.Builder(context)
                    .build().apply {
                        //gắn exo cho player view
                        playerView.get()?.player = this
                        playerView.get()?.useController = useController

                        //load thumb
                        thumbnail?.get()?.let { glide?.load(thumbSource)?.into(it) }

                        //callback
                        registerPlayerListener(
                            position = position,
                            exoPlayer = this,
                            thumbnail = thumbnail,
                            loadingView = loadingView,
                            isMoveToNext = isMoveToNext,
                            listener = listener
                        )

                        //tạo media source
                        val mediaSource = buildMediaSource(Uri.parse(source))
                        if (mediaSource != null) {
                            this.setMediaSource(mediaSource)
                            playWhenReady = position == this@ExoProvider.currentPosition
                            prepare()

                            //add vào pool
                            addToExoPool(position, this)
                        }
                    }
            } else {
                //load lại thumb trong trường hợp view bị recycled
                thumbnail?.get()?.let { glide?.load(thumbSource)?.into(it) }
                //player view cần được gắn lại exo trường hợp view bị recycled
                playerView.get()?.player = exoPlayers[position]
                playerView.get()?.hideController()

                registerPlayerListener(
                    position = position,
                    exoPlayer = exoPlayers[position]!!,
                    thumbnail = thumbnail,
                    loadingView = loadingView,
                    isMoveToNext = isMoveToNext,
                    listener = listener
                )
            }
        }
//        }

//        Log.d(TAG, "setupWith: Done after $time")

    }

    private fun registerPlayerListener(
        position: Int,
        exoPlayer: SimpleExoPlayer,
        thumbnail: WeakReference<AppCompatImageView>?,
        loadingView: WeakReference<View>?,
        isMoveToNext: Boolean,
        listener: ExoController.HnimExoPlayerListener
    ) {
        exoPlayer.addListener(object : EventListener {
            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                Log.e(
                    TAG,
                    "onPlayerError: $position - $error - ${error.unexpectedException.toString()}"
                )
                if (position == currentPosition) {
                    loadingView?.get()?.visibility = View.GONE
                }
                listener.onError(error)
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                when (state) {
                    STATE_BUFFERING -> {
                        if (position == currentPosition) {
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
                if (isPlaying && currentPosition == position)
                    thumbnail?.get()?.visibility = View.GONE
                else if (!isPlaying && currentPosition != position)
                    thumbnail?.get()?.visibility = View.VISIBLE
            }
        })
    }

    fun setExoPool(exoPool: Int) {
        this.exoPool = exoPool
    }

    fun exoPlayers() = exoPlayers

    fun setCurrentPosition(position: Int) {
        this.currentPosition = position
    }

    //add exo player vào pool, nếu pool đầy xóa phần tử xa nhất
    private fun addToExoPool(tagPosition: Int, player: SimpleExoPlayer) {
        Log.d(TAG, "addToExoPool: position $tagPosition")

        if (tagPosition > maxPosition)
            maxPosition = tagPosition
        if (tagPosition < minPosition)
            minPosition = tagPosition

        //nếu pool đã max -> xóa phần tử xa nhất
        if (exoPlayers.size >= exoPool) {
            removeItemInPool(tagPosition)
        }

        exoPlayers[tagPosition] = player

        Log.d(TAG, "list left: ${exoPlayers.keys}")
    }

    private fun removeItemInPool(tagPosition: Int) {
        findFurthestPosition(tagPosition).let { furthestPos ->
            exoPlayers[furthestPos]?.release()
            exoPlayers.remove(furthestPos)
        }
    }

    //tìm vị trí xa nhất so với vị trí hiện tại
    private fun findFurthestPosition(tagPosition: Int): Int {
        val minValue = minPosition
        val maxValue = maxPosition
        val centerValue = (maxValue + minValue) / 2f

        Log.d(TAG, "findFurthestPosition: $currentPosition - $minPosition - $maxPosition")

        return if (tagPosition >= centerValue) {
            //vị trí xa nhất là vị trí nhỏ nhất
            minPosition++
            minValue
        } else {
            //vị trí xa nhất là vị trí lớn nhất
            maxPosition--
            maxValue
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource? {
        context.get()?.let {
            val type = Util.inferContentType(uri)
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                it, Util.getUserAgent(it, it.packageName ?: "Gapo")
            )
            val mediaItem = MediaItem.fromUri(uri)
            return when (type) {
//                C.TYPE_DASH -> com.google.android.exoplayer2.source.dash.DashMediaSource.Factory(
//                    dataSourceFactory
//                ).createMediaSource(mediaItem)
//                C.TYPE_SS -> com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource.Factory(
//                    dataSourceFactory
//                ).createMediaSource(mediaItem)
                C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true) //faster start-up times
                    .createMediaSource(mediaItem)
                else -> DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)
            }
        } ?: return null
    }

    companion object {
        private const val TAG = "HnimExo::ExoProvider"
    }
}
