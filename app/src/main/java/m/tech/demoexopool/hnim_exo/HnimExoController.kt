package m.tech.demoexopool.hnim_exo

import android.view.View
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author: 89hnim
 * @since: 12/04/2021
 */
interface HnimExoController {

    fun setupWith(
        position: Int,
        source: String,
        preloadSource: Array<String?>?,
        preloadImageSource: Array<String?>?,
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