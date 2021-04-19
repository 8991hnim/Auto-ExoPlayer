package m.tech.demoexopool.hnim_exo

import android.content.Context
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gg.gapo.video.hnim_exo.ExoController
import m.tech.demoexopool.VideoAdapter
import m.tech.demoexopool.hnim_exo.BusEven.HE_MOVE_TO_NEXT
import org.simple.eventbus.EventBus
import org.simple.eventbus.Subscriber

/**
 * @author: 89hnim
 * @since: 12/04/2021
 */
class HnimExo(
    private val controller: ExoController,
    lifeCycle: Lifecycle?
) {

    private var vp2: ViewPager2? = null

    fun getExoController(): HnimExoController = controller

    private val onPageChange = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            controller.togglePlayer(
                position,
                ((vp2?.get(0) as RecyclerView).findViewHolderForAdapterPosition(position) as VideoAdapter.VideoHolder).getPlayerView()
            )
        }
    }

    fun attach(vp2: ViewPager2) {
        this.vp2 = vp2
        this.vp2?.registerOnPageChangeCallback(onPageChange)
    }

    init {
        lifeCycle?.launchWhenResumed {
            this.vp2?.currentItem?.let { controller.play(it) }
            return@launchWhenResumed true
        }

        lifeCycle?.launchWhenStarted {
            EventBus.getDefault().register(this)
            return@launchWhenStarted true
        }

        lifeCycle?.launchWhenStopped {
            controller.pauseAll()
            EventBus.getDefault().unregister(this)
            return@launchWhenStopped true
        }

        lifeCycle?.launchWhenDestroyed {
            onDestroy()
            return@launchWhenDestroyed false
        }
    }

    fun onDestroy() {
        clear(true)
    }

    fun clear(isDestroy: Boolean) {
        if (isDestroy) {
//            SimpleCacheFactory.clearCache()
            vp2?.unregisterOnPageChangeCallback(onPageChange)
            vp2 = null
        }
        controller.clear(isDestroy)
    }

    fun onPause() {
        controller.pauseAll()
    }

    fun onResume() {
        this.vp2?.currentItem?.let {
            controller.play(it)
        }
    }

    /**
     * Prevent callback twice b/c of not properly exo player callback
     * Need fix later
     */
    private var lastTimeMoveToNext = 0L

    @Subscriber(tag = HE_MOVE_TO_NEXT)
    fun onMoveToNextVideo(ev: Int) {
        this.vp2?.let { vp2 ->
            if (System.currentTimeMillis() - lastTimeMoveToNext < 1000) return@let
            lastTimeMoveToNext = System.currentTimeMillis()
            vp2.setCurrentItem(vp2.currentItem + 1, true)
        }
    }

    class Builder(private val context: Context) {
        private var lifeCycle: Lifecycle? = null
        private val controller = ExoController(context)

        fun lifeCycle(lifeCycle: Lifecycle): Builder {
            this.lifeCycle = lifeCycle
            return this
        }

        fun autoPlay(autoPlay: Boolean): Builder {
            this.controller.setAutoPlay(autoPlay)
            return this
        }

        fun isMuted(isMuted: Boolean): Builder {
            this.controller.setMuted(0, isMuted)
            return this
        }

        fun isSaveState(isSaveState: Boolean): Builder {
            this.controller.setSaveState(isSaveState)
            return this
        }

        fun autoMoveNext(isAutoMoveNext: Boolean): Builder {
            this.controller.setAutoMoveNext(isAutoMoveNext)
            return this
        }

        fun create(): HnimExo {
            SimpleCacheFactory.init(context)
            return HnimExo(controller, lifeCycle)
        }

    }

    companion object {
        private const val TAG = "HnimExo::HnimExo"
    }

}