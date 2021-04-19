package m.tech.demoexopool.hnim_exo

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.gg.gapo.video.hnim_exo.ExoController
import com.google.android.exoplayer2.ui.PlayerView

interface HnimExoController {

    fun setupWith(
        position: Int,
        source: String,
        preloadSource: Array<String?>?,
        thumbSource: String?,
        thumbnail: AppCompatImageView?,
        loadingView: View?,
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