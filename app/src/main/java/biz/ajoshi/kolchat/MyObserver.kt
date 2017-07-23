package biz.ajoshi.kolchat

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent

/**
 * Created by ajoshi on 7/22/17. Does absolutely nothing right now. Might not even have a use (but maybe to kill chatservice)
 */
class MyObserver : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {

    }
}
