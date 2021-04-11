package m.tech.demoexopool.exo

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import java.lang.ref.WeakReference

class HnimExo(
    private val controller: ExoController,
    lifeCycle: Lifecycle?
) {

    private var vp2: WeakReference<ViewPager2>? = null

    fun getExoController(): HnimExoController = controller

    fun attach(vp2: ViewPager2) {
        this.vp2 = WeakReference(vp2)
        this.vp2?.get()?.let {
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    controller.togglePlayer(position)
                }
            })

            //tính lại exo pool nếu offscreen limit không phải default
            //k nên set offscreenPageLimit vì exoPool sẽ rất lớn
            if (it.offscreenPageLimit != ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT)
                controller.setExoPool(it.offscreenPageLimit * 2 + 1 + it.offscreenPageLimit)
        }
    }

    init {
        lifeCycle?.launchWhenStopped {
            controller.pauseAll()
            return@launchWhenStopped true
        }

        lifeCycle?.launchWhenResumed {
            this.vp2?.get()?.currentItem?.let { controller.play(it) }
            return@launchWhenResumed true
        }
    }

    class Builder(context: Context) {
        private var exoPool = DEFAULT_EXO_POOL
        private var lifeCycle: Lifecycle? = null
        private val controller = ExoController(exoPool, context)

        fun exoPool(pool: Int): Builder {
            if (pool < MIN_EXO_POOL) {
                throw IllegalArgumentException("exoPool must be a number > 3")
            }
            this.exoPool = pool
            return this
        }

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

        fun create(): HnimExo {
            return HnimExo(controller, lifeCycle)
        }

    }

    companion object {
        private const val DEFAULT_EXO_POOL = 5
        private const val MIN_EXO_POOL = 4 //chưa rõ offscreenlimit default của vp2, cần check lại
        private const val TAG = "HnimExo"
    }

}