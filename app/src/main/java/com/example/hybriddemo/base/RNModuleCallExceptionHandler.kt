package com.example.hybriddemo.base

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.JSExceptionHandler
import com.facebook.react.bridge.UiThreadUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class RNModuleCallExceptionHandler(val application: ReactApplication?) :
    JSExceptionHandler {

    private val reDrawNum = AtomicInteger(0)

    companion object {
        internal const val MAX_RE_DRAW_NUM = 2
        const val TAG = "RNError"
    }

    override fun handleException(p0: Exception?) {
        // 异常收集
        val reDrawNum = this.reDrawNum.incrementAndGet()
        if (reDrawNum > MAX_RE_DRAW_NUM) {
            Log.e(TAG, "max re draw")
            return
        }
        val currentActivity = if (UiThreadUtil.isOnUiThread()) {
            application?.reactNativeHost?.reactInstanceManager?.currentReactContext?.currentActivity
        } else {
             null
        }
        if (currentActivity != null && currentActivity is FragmentActivity) {
            currentActivity.lifecycleScope.launch(Dispatchers.Main) {
                application?.reactNativeHost?.clear()
                //重新创建一个RN实例管理器
                application?.reactNativeHost?.reactInstanceManager
//                XBus.get(null).of(RNErrEvent::class.java).post(RNErrEvent())
//                MainOpenEntrance.startIndex()// 如果 rn 异常了，导航到一个可展示的原生页面；
            }
        }
    }

}