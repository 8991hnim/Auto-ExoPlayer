package m.tech.demoexopool.exo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import java.lang.ref.WeakReference

/**
 * @author: 89hnim
 * @since: 12/04/2021
 */
class ExoProvider
constructor(
    private var exoPool: Int,
    private val context: WeakReference<Context>
) {

    private val glide by lazy { context.get()?.let { Glide.with(it) } }
    private val exoPlayers = HashMap<Int, SimpleExoPlayer>()
    private var maxPosition = 0
    private var minPosition = Integer.MAX_VALUE
    private var currentPosition = 0

    //hàm prepare exo
    fun setupWith(
        position: Int,
        source: String,
        thumbSource: String?,
        thumbnail: WeakReference<AppCompatImageView>?,
        useController: Boolean,
        playerView: WeakReference<PlayerView>,
        listener: ExoController.HnimExoPlayerListener
    ) {
        Log.d(TAG, "setupWith: $position - ${playerView.hashCode()}")
        context.get()?.let { context ->
            if (exoPlayers[position] == null) {
                SimpleExoPlayer.Builder(context).build().apply {
                    //gắn exo cho player view
                    playerView.get()?.player = this
                    playerView.get()?.useController = useController

                    //callback
                    registerPlayerListener(this, thumbSource, thumbnail, listener)

                    //tạo exo
                    val mediaItem = MediaItem.fromUri(Uri.parse(source))
                    this.setMediaItem(mediaItem)
                    prepare()

                    //add vào pool
                    addToExoPool(position, this)
                }
            } else {
                //trường hợp view bị recycled, player view cần được gắn lại exo
                registerPlayerListener(exoPlayers[position]!!, thumbSource, thumbnail, listener)
                playerView.get()?.player = exoPlayers[position]
            }
        }
    }

    private fun registerPlayerListener(
        exoPlayer: SimpleExoPlayer,
        thumbSource: String?,
        thumbnail: WeakReference<AppCompatImageView>?,
        listener: ExoController.HnimExoPlayerListener
    ) {
        exoPlayer.addListener(object : EventListener {
            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                listener.onError(error)
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                when (state) {
                    STATE_BUFFERING -> {
                        thumbnail?.get()?.let { thumbnail ->
                            thumbnail.isVisible = true
                            glide?.load(thumbSource)?.into(thumbnail)
                        }
                        listener.onBuffering()
                    }
                    STATE_READY -> {
                        thumbnail?.get()?.isVisible = false
                        listener.onReady()
                    }
                    STATE_ENDED -> listener.onEnded()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                listener.onPlayingChanged(isPlaying)
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
        exoPlayers[tagPosition] = player

        if (tagPosition > maxPosition)
            maxPosition = tagPosition
        if (tagPosition < minPosition)
            minPosition = tagPosition

        //nếu pool đã max -> xóa phần tử xa nhất
        if (exoPlayers.size > exoPool) {
            removeItemInPool()
        }
    }

    private fun removeItemInPool() {
        findFurthestPosition().let { furthestPos ->
            exoPlayers[furthestPos]?.release()
            exoPlayers.remove(furthestPos)

            Log.d(TAG, "removeItemInPool: $furthestPos - list left: ${exoPlayers.keys}")
        }
    }

    //tìm vị trí xa nhất so với vị trí hiện tại
    private fun findFurthestPosition(): Int {
        val minValue = minPosition
        val maxValue = maxPosition
        val centerValue = (maxValue + minValue) / 2f

        Log.d(TAG, "findFurthestPosition: $currentPosition - $minPosition - $maxPosition")

        return if (currentPosition >= centerValue) {
            //vị trí xa nhất là vị trí nhỏ nhất
            minPosition++
            minValue
        } else {
            //vị trí xa nhất là vị trí lớn nhất
            maxPosition--
            maxValue
        }
    }

    companion object {
        private const val TAG = "HnimExo::ExoProvider"
    }
}
