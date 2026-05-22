package com.example.hybriddemo.rn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.hybriddemo.HotUpdateManager
import com.example.hybriddemo.ui.NativeDetailActivity
import com.example.hybriddemo.ui.NativeInputActivity
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * HybridBridgeModule - 核心桥接模块（RN → 原生）
 *
 * 对应 RN 侧的 NativeModules.HybridBridge。
 * 提供页面跳转、数据存储、设备信息、热更新等能力。
 *
 * 参考 yp_rn_app 中的 YPModule、YPRouterModule、YPStoreModule 的设计，
 * 这里合并为一个模块简化演示。
 */
class HybridBridgeModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val MODULE_NAME = "HybridBridge"
        const val SHARED_PREFS_NAME = "hybrid_shared_data"
    }

    override fun getName(): String = MODULE_NAME

    // ==================== 页面跳转 ====================

    /**
     * RN → 原生页面跳转
     *
     * 参考 yp_rn_app 中的 YPRouterModule.goNative()
     * 实际项目中使用 ARouter 等路由框架分发
     */
    @ReactMethod
    fun openNativePage(pageName: String, params: ReadableMap) {
        val activity = currentActivity ?: return
        val intent = when (pageName) {
            "NativeDetailPage" -> Intent(activity, NativeDetailActivity::class.java)
            "NativeInputPage" -> Intent(activity, NativeInputActivity::class.java)
            else -> return
        }
        intent.putExtras(toBundle(params))
        activity.startActivity(intent)
    }

    /**
     * RN → 原生页面（等待返回值）
     *
     * 参考 yp_rn_app 中的 YPRouterModule.openNewActivityResult()
     */
    @ReactMethod
    fun openNativePageForResult(pageName: String, params: ReadableMap, promise: Promise) {
        val activity = currentActivity ?: run {
            promise.reject("NO_ACTIVITY", "No activity available")
            return
        }
        val intent = when (pageName) {
            "NativeInputPage" -> Intent(activity, NativeInputActivity::class.java)
            else -> {
                promise.reject("UNKNOWN_PAGE", "Unknown page: $pageName")
                return
            }
        }
        intent.putExtras(toBundle(params))

        val listener = object : BaseActivityEventListener() {
            override fun onActivityResult(act: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
                reactContext.removeActivityEventListener(this)
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = WritableNativeMap().apply {
                        putString("input", data.getStringExtra("result_input") ?: "")
                        putInt("code", resultCode)
                    }
                    promise.resolve(result)
                } else {
                    promise.reject("CANCELLED", "用户取消操作")
                }
            }
        }
        reactContext.addActivityEventListener(listener)
        activity.startActivityForResult(intent, 1001)
    }

    /**
     * 打开新的 RN 页面
     *
     * 参考 yp_rn_app 中的 YPRouterModule.openNewActivity()
     * 原生新开一个 RNContainerActivity 承载指定的 RN 组件
     */
    @ReactMethod
    fun openRNPage(viewName: String, params: ReadableMap) {
        val activity = currentActivity ?: return
        val bundle = toBundle(params)
        RNContainerActivity.start(activity, viewName, bundle)
    }

    /** 关闭当前页面 */
    @ReactMethod
    fun closePage() {
        currentActivity?.finish()
    }

    /** 关闭并返回数据 */
    @ReactMethod
    fun closePageWithResult(resultJson: String) {
        val activity = currentActivity
        if (activity is RNContainerActivity) {
            activity.setResultAndFinish(resultJson)
        } else {
            val intent = Intent().apply {
                putExtra("result_data", resultJson)
            }
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }
    }

    /** 显示原生 Toast */
    @ReactMethod
    fun showToast(message: String) {
        val activity = currentActivity ?: return
        activity.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== 数据持久化 ====================

    /**
     * 保存数据到原生存储
     *
     * 参考 yp_rn_app 中的 YPStoreModule.setStore()
     * 实际项目使用 MMKV，这里用 SharedPreferences 简化演示
     */
    @ReactMethod
    fun saveData(key: String, value: String, promise: Promise) {
        try {
            val prefs = reactContext.getSharedPreferences(SHARED_PREFS_NAME, 0)
            prefs.edit().putString(key, value).apply()
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("SAVE_ERROR", e.message)
        }
    }

    /** 从原生存储读取数据 */
    @ReactMethod
    fun getData(key: String, promise: Promise) {
        try {
            val prefs = reactContext.getSharedPreferences(SHARED_PREFS_NAME, 0)
            promise.resolve(prefs.getString(key, null))
        } catch (e: Exception) {
            promise.reject("READ_ERROR", e.message)
        }
    }

    /** 删除数据 */
    @ReactMethod
    fun removeData(key: String, promise: Promise) {
        try {
            val prefs = reactContext.getSharedPreferences(SHARED_PREFS_NAME, 0)
            prefs.edit().remove(key).apply()
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("REMOVE_ERROR", e.message)
        }
    }

    // ==================== 设备信息 ====================

    /** 获取设备信息 */
    @ReactMethod
    fun getDeviceInfo(promise: Promise) {
        try {
            val info = WritableNativeMap().apply {
                putString("model", android.os.Build.MODEL)
                putString("systemVersion", "Android ${android.os.Build.VERSION.RELEASE}")
                putString("appVersion", reactContext.packageManager
                    .getPackageInfo(reactContext.packageName, 0).versionName ?: "1.0.0")
            }
            promise.resolve(info)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }

    // ==================== 热更新 ====================

    /** 获取当前 Bundle 信息 */
    @ReactMethod
    fun getBundleInfo(promise: Promise) {
        val info = HotUpdateManager.getBundleInfo(reactContext)
        val result = WritableNativeMap().apply {
            putString("version", info.version)
            putString("source", info.source)
            putString("updateTime", info.updateTime)
        }
        promise.resolve(result)
    }

    /** 检查热更新 */
    @ReactMethod
    fun checkHotUpdate(promise: Promise) {
        val result = HotUpdateManager.checkUpdate(reactContext)
        val map = WritableNativeMap().apply {
            putBoolean("hasUpdate", result.hasUpdate)
            putString("message", result.message)
        }
        promise.resolve(map)
    }

    // ==================== 原生 → RN 事件发送 ====================

    /**
     * 向 RN 发送事件
     *
     * 参考 yp_rn_app 中的 RNEventEmitter.sendEvent()
     * 原生侧主动通知 RN 的方式
     */
    fun sendEventToRN(eventName: String, params: WritableMap?) {
        if (reactContext.hasActiveReactInstance()) {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        }
    }

    @ReactMethod
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Int) {}

    // ==================== 工具方法 ====================

    private fun toBundle(map: ReadableMap): Bundle {
        val bundle = Bundle()
        val iter = map.keySetIterator()
        while (iter.hasNextKey()) {
            val key = iter.nextKey()
            when (map.getType(key)) {
                ReadableType.String -> bundle.putString(key, map.getString(key))
                ReadableType.Number -> bundle.putDouble(key, map.getDouble(key))
                ReadableType.Boolean -> bundle.putBoolean(key, map.getBoolean(key))
                else -> {}
            }
        }
        return bundle
    }
}
