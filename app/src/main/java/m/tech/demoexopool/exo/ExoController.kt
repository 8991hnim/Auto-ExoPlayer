package m.tech.demoexopool.exo

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.exoplayer2.ui.PlayerView
import java.lang.ref.WeakReference

class ExoController(
    private val exoPool: Int,
    private val context: Context
) : HnimExoController {

    private var autoPlay: Boolean = false
    private var isMuted: Boolean = false

    private val exoProvider: ExoProvider by lazy {
        ExoProvider(exoPool, WeakReference(context))
    }

    fun togglePlayer(currentPosition: Int) {
        exoProvider.setCurrentPosition(currentPosition)
        exoProvider.togglePlayer(autoPlay = autoPlay, isMuted = isMuted)
    }

    fun setExoPool(exoPool: Int) {
        exoProvider.setExoPool(exoPool)
    }

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
        listener: ExoProvider.HnimExoPlayerListener
    ) {
        exoProvider.setupWith(
            position = position,
            source = source,
            thumbSource = thumbSource,
            thumbnail = thumbnail,
            useController = useController,
            playerView = playerView,
            listener = listener
        )
    }

    override fun play(position: Int) {
        exoProvider.exoPlayers()[position]?.play()
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


}