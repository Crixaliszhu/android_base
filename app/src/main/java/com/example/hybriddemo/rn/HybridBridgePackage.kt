package com.example.hybriddemo.rn

import com.example.hybriddemo.rn.view.CircleProgressViewManager
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * NativeModule 注册包
 *
 * 参考 yp_rn_app 中的 RNMainPackage：
 * - 注册所有自定义 NativeModule
 * - 注册自定义 ViewManager（如果有原生 View 组件）
 *
 * 在 MainApplication.getPackages() 中添加此包。
 */
class HybridBridgePackage : ReactPackage {

    override fun createNativeModules(
        reactContext: ReactApplicationContext
    ): List<NativeModule> {
        return listOf(
            HybridBridgeModule(reactContext),  // RN → 原生（页面跳转、存储、设备信息）
            CallRNModule(reactContext)          // 原生 → RN（调用 RN 逻辑方法）
        )
    }

    override fun createViewManagers(
        reactContext: ReactApplicationContext
    ): List<ViewManager<*, *>> {
        return listOf(
            CircleProgressViewManager()  // 原生圆形进度条 → RN 通过 requireNativeComponent("RNCircleProgress") 使用
        )
    }
}
