package m.tech.demoexopool.hnim_exo

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * @author: 89hnim
 * @since: 13/04/2021
 *  The boolean value:
 *  true: Continue observe
 *  false: Stop observe
 */
fun Lifecycle.launchWhenStarted(action: () -> Boolean) {
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_START)
                if(!action()){
                    removeObserver(this)
                }
        }
    })
}

fun Lifecycle.launchWhenResumed(action: () -> Boolean) {
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_RESUME)
                if(!action()){
                    removeObserver(this)
                }
        }
    })
}

fun Lifecycle.launchWhenPaused(action: () -> Boolean) {
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_PAUSE)
                if(!action()){
                    removeObserver(this)
                }
        }
    })
}

fun Lifecycle.launchWhenStopped(action: () -> Boolean) {
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_STOP)
                if(!action()){
                    removeObserver(this)
                }
        }
    })
}

fun Lifecycle.launchWhenDestroyed(action: () -> Boolean) {
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY)
                if(!action()){
                    removeObserver(this)
                }
        }
    })
}