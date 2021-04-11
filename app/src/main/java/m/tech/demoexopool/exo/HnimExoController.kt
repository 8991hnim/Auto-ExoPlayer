package m.tech.demoexopool.exo

import androidx.appcompat.widget.AppCompatImageView
import com.google.android.exoplayer2.ui.PlayerView

interface HnimExoController {

    fun setupWith(
        position: Int,
        source: String,
        thumbSource: String?,
        useController: Boolean,
        playerView: PlayerView,
        thumbnail: AppCompatImageView?,
        listener: ExoController.HnimExoPlayerListener
    )

    fun play(position: Int)

    fun pause(position: Int)

    fun stop(position: Int)

    fun seekTo(position: Int, positionMs: Long)

    fun setAutoPlay(autoPlay: Boolean)

    fun setMuted(position: Int, isMuted: Boolean)

    fun setSaveState(saveState: Boolean)

}