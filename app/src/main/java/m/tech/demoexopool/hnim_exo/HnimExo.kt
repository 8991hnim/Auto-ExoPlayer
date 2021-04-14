package m.tech.demoexopool.hnim_exo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import m.tech.demoexopool.hnim_exo.BusEven.HE_MOVE_TO_NEXT
import org.simple.eventbus.EventBus
import org.simple.eventbus.Subscriber
import java.lang.ref.WeakReference

/**
 * @author: 89hnim
 * @since: 12/04/2021
 */
class HnimExo(
    private val controller: ExoController,
    lifeCycle: Lifecycle?
) {

    private var vp2: WeakReference<ViewPager2>? = null
    private val handler = Handler(Looper.getMainLooper())

    fun getExoController(): HnimExoController = controller

    fun attach(vp2: ViewPager2) {
        this.vp2 = WeakReference(vp2)
        this.vp2?.get()?.let {
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    /*
                    setOffscreenPageLimit có vấn đề:
                    Dù kéo hết offscreenPageLimit, ví dụ: offscreenPageLimit = 2.
                    Scroll từ b1 -> b2 -> b3 -> b4 -> b5: Vị trí 0 k đc bind lại làm k setup lai đc player
                    (trước đó player ở 0 đã bị xóa khi kéo đến vị trí thứ 3)
                    0       1         2         3          4         5
                    b1      b2       b3         b4
        b7[ko đc bind lại]  b6       b5
                    --> tạm thời check nếu scroll đến page mà tại page đó player chưa đc init thì init
                     */
                    handler.removeCallbacksAndMessages(null)
                    handler.post {
                        if (!controller.isPlayerExist(position)) {
                            Log.d(TAG, "onPageSelected: rebind $position")
                            it.adapter?.notifyItemChanged(position)
                        }
                        controller.togglePlayer(position)
                    }

                }
            })

            /*
            cần có công thức tính exo pool từ offscreenPageLimit chuẩn hơn
            lí do: khi vp2 scroll quá offscreenPageLimit view không đc bind lại (như trên)
            */
            if (it.offscreenPageLimit != ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT) {
                /**
                 *  tạm thời set lại là 2 vì để
                 *  k tốn tài nguyên load trước nhiều video có thể k xem đến
                 */
                it.offscreenPageLimit = 2
                var newPool = it.offscreenPageLimit * 2 + 1
                if(newPool < 5) newPool = 5
                controller.setExoPool(newPool)
            }

        }
    }

    init {
        lifeCycle?.launchWhenResumed {
            this.vp2?.get()?.currentItem?.let { controller.play(it) }
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
        clear()
    }

    fun clear() {
        controller.clear()
    }

    fun onPause() {
        controller.pauseAll()
    }

    fun onResume() {
        this.vp2?.get()?.currentItem?.let {
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
        this.vp2?.get()?.let { vp2 ->
            if (System.currentTimeMillis() - lastTimeMoveToNext < 1000) return@let
            lastTimeMoveToNext = System.currentTimeMillis()
            vp2.setCurrentItem(vp2.currentItem + 1, true)
        }
    }

    class Builder(context: Context) {
        private var lifeCycle: Lifecycle? = null
        private val controller = ExoController(DEFAULT_EXO_POOL, context)

        fun exoPool(pool: Int): Builder {
            if (pool < 1) {
                throw IllegalArgumentException("exoPool must be a number > 0")
            }
            controller.setExoPool(pool)
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

        fun isSaveState(isSaveState: Boolean): Builder {
            this.controller.setSaveState(isSaveState)
            return this
        }

        fun autoMoveNext(isAutoMoveNext: Boolean): Builder {
            this.controller.setAutoMoveNext(isAutoMoveNext)
            return this
        }

        fun create(): HnimExo {
            return HnimExo(controller, lifeCycle)
        }

    }

    companion object {
        private const val DEFAULT_EXO_POOL = 5
        private const val TAG = "HnimExo::HnimExo"
    }

}