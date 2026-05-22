package com.example.hybriddemo.rn

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * CallRNModule - 原生调用 RN 的桥接模块
 *
 * 参考 yp_rn_app 中的 RNCallClient / RNCallCore 设计：
 * 1. 原生通过 callRN() 发起调用
 * 2. 通过 RCTDeviceEventEmitter 发送 "call_rn" 事件
 * 3. RN 监听事件，执行对应 module.method
 * 4. RN 通过 receiveRNResult/receiveRNError 回传结果
 * 5. 原生通过回调获取结果
 *
 * 这种设计让原生可以复用 RN 侧的业务逻辑（如数据格式化、计算等），
 * 避免两端重复实现。
 */
class CallRNModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val MODULE_NAME = "CallRNModule"

        /** 等待 RN 回调的 Promise 缓存 */
        private val pendingCallbacks = ConcurrentHashMap<String, (String?) -> Unit>()
        private var callCounter = 0L

        /**
         * 原生调用 RN 方法
         *
         * @param reactContext React 上下文
         * @param moduleName RN 模块名（如 "math", "data"）
         * @param methodName 方法名（如 "add", "formatPrice"）
         * @param params 参数
         * @param callback 回调（返回 JSON 字符串或 null）
         *
         * 使用示例：
         * ```kotlin
         * CallRNModule.callRN(reactContext, "math", "add", mapOf("a" to 1, "b" to 2)) { result ->
         *     Log.d("CallRN", "结果: $result")
         * }
         * ```
         */
        fun callRN(
            reactContext: ReactApplicationContext,
            moduleName: String,
            methodName: String,
            params: Map<String, Any>? = null,
            callback: ((String?) -> Unit)? = null
        ) {
            if (!reactContext.hasActiveReactInstance()) {
                callback?.invoke(null)
                return
            }

            val callId = "call_${++callCounter}_${System.currentTimeMillis()}"

            // 注册回调
            if (callback != null) {
                pendingCallbacks[callId] = callback
            }

            // 构建事件数据
            val eventData = WritableNativeMap().apply {
                val event = WritableNativeMap().apply {
                    putString("call_id", callId)
                    putString("module_name", moduleName)
                    putString("method", methodName)
                }
                putMap("event", event)
                putString("query", JSONObject(params ?: emptyMap<String, Any>()).toString())
            }

            // 发送事件给 RN
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("call_rn", eventData)
        }
    }

    override fun getName(): String = MODULE_NAME

    /**
     * RN 回传执行结果
     */
    @ReactMethod
    fun receiveRNResult(event: ReadableMap, resultJson: String) {
        val callId = event.getString("call_id") ?: return
        pendingCallbacks.remove(callId)?.invoke(resultJson)
    }

    /**
     * RN 回传执行错误
     */
    @ReactMethod
    fun receiveRNError(event: ReadableMap, errCode: String, errMsg: String) {
        val callId = event.getString("call_id") ?: return
        pendingCallbacks.remove(callId)?.invoke(null)
    }

    @ReactMethod
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Int) {}
}
