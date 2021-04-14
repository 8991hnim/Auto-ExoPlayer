package m.tech.demoexopool.hnim_exo

import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ui.PlayerView
import java.lang.ref.WeakReference

/**
 * @author: 89hnim
 * @since: 12/04/2021
 */
class ExoController(
    private val exoPool: Int,
    private val context: Context
) : HnimExoController {

    private var autoPlay: Boolean = false
    private var isMuted: Boolean = false
    private var isSaveState: Boolean = false
    private var isAutoMoveNext: Boolean = false

    private val exoProvider: ExoProvider2 by lazy {
        ExoProvider2(exoPool, WeakReference(context))
    }

    fun isPlayerExist(position: Int): Boolean{
        return  exoProvider.exoPlayers()[position] != null
    }

    //hàm thật sự play video
    fun togglePlayer(currentPosition: Int) {
        Log.d(TAG, "togglePlayer: $currentPosition")

        exoProvider.setCurrentPosition(currentPosition)

        exoProvider.exoPlayers().forEach {
            if (!isSaveState) {
                it.value.seekTo(0)
            }
            it.value.volume = if (isMuted) 0f else 1f

            if (it.key == currentPosition) {
                it.value.playWhenReady = autoPlay
            } else {
                it.value.playWhenReady = false
            }
        }
    }

    //gọi nếu offscreenlimit của vp2 khác mặc định để set lại pool
    fun setExoPool(exoPool: Int) {
        Log.d("MTEST", "called controoler:  $exoPool")
        exoProvider.setExoPool(exoPool)
    }

    //gọi khi lifecycle onPause
    fun pauseAll() {
        exoProvider.exoPlayers().values.forEach {
            it.playWhenReady = false
        }
    }

    override fun setupWith(
        position: Int,
        source: String,
        thumbSource: String?,
        useController: Boolean,
        playerView: PlayerView,
        thumbnail: AppCompatImageView?,
        loadingView: View?,
        listener: HnimExoPlayerListener
    ) {
        exoProvider.setupWith(
            position = position,
            source = source,
            thumbSource = thumbSource,
            thumbnail = if (thumbnail != null) WeakReference(thumbnail) else null,
            loadingView = if (loadingView != null) WeakReference(loadingView) else null,
            useController = useController,
            playerView = WeakReference(playerView),
            isMoveToNext = isAutoMoveNext,
            listener = listener
        )
    }

    override fun play(position: Int) {
        exoProvider.exoPlayers()[position]?.playWhenReady = true
    }

    override fun pause(position: Int) {
        exoProvider.exoPlayers()[position]?.pause()
    }

    override fun stop(position: Int) {
        exoProvider.exoPlayers()[position]?.stop()
    }

    override fun seekTo(position: Int, positionMs: Long) {
        exoProvider.exoPlayers()[position]?.seekTo(positionMs)
    }

    override fun setAutoPlay(autoPlay: Boolean) {
        this.autoPlay = autoPlay
    }

    override fun setMuted(position: Int, isMuted: Boolean) {
        this.isMuted = isMuted
        exoProvider.exoPlayers()[position]?.volume = if (isMuted) 0f else 1f
    }

    override fun setSaveState(saveState: Boolean) {
        this.isSaveState = saveState
    }

    fun setAutoMoveNext(isAutoMoveNext: Boolean) {
        this.isAutoMoveNext = isAutoMoveNext
    }

    fun clear(){
        exoProvider.clear()
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
