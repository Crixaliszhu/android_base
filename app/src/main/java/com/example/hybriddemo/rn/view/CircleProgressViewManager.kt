package com.example.hybriddemo.rn.view

import android.graphics.Color
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

/**
 * CircleProgressView 的 ViewManager
 *
 * ViewManager 是 RN 使用原生 View 的桥梁：
 * 1. 定义组件名（getName）→ RN 侧通过 requireNativeComponent(name) 获取
 * 2. 创建 View 实例（createViewInstance）
 * 3. 通过 @ReactProp 注解暴露可控属性 → RN 侧通过 props 传入
 *
 * 注册方式：
 * 在 HybridBridgePackage.createViewManagers() 中添加此 ViewManager 实例。
 *
 * RN 侧使用：
 * ```typescript
 * import { requireNativeComponent } from 'react-native';
 * const NativeCircleProgress = requireNativeComponent('RNCircleProgress');
 * <NativeCircleProgress style={{ width: 120, height: 120 }} progress={75} color="#FF6B6B" />
 * ```
 */
class CircleProgressViewManager : SimpleViewManager<CircleProgressView>() {

    companion object {
        /** 组件名 - RN 侧通过此名称引用 */
        const val REACT_CLASS = "RNCircleProgress"
    }

    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(reactContext: ThemedReactContext): CircleProgressView {
        return CircleProgressView(reactContext)
    }

    // ==================== @ReactProp 暴露属性给 RN ====================

    /**
     * 进度值 (0~100)
     * RN: <NativeCircleProgress progress={75} />
     */
    @ReactProp(name = "progress", defaultInt = 0)
    fun setProgress(view: CircleProgressView, progress: Int) {
        view.progress = progress
    }

    /**
     * 进度条颜色
     * RN: <NativeCircleProgress color="#FF6B6B" />
     */
    @ReactProp(name = "color")
    fun setColor(view: CircleProgressView, color: String?) {
        if (!color.isNullOrBlank()) {
            try {
                view.progressColor = Color.parseColor(color)
            } catch (_: IllegalArgumentException) {
                // 颜色解析失败，保持默认
            }
        }
    }

    /**
     * 轨道颜色
     * RN: <NativeCircleProgress trackColor="#F0F0F0" />
     */
    @ReactProp(name = "trackColor")
    fun setTrackColor(view: CircleProgressView, color: String?) {
        if (!color.isNullOrBlank()) {
            try {
                view.trackColor = Color.parseColor(color)
            } catch (_: IllegalArgumentException) {
                // ignore
            }
        }
    }

    /**
     * 进度条宽度
     * RN: <NativeCircleProgress strokeWidth={8} />
     */
    @ReactProp(name = "strokeWidth", defaultFloat = 12f)
    fun setStrokeWidth(view: CircleProgressView, width: Float) {
        val density = view.context.resources.displayMetrics.density
        view.strokeWidth = width * density  // dp → px
    }

    /**
     * 是否显示百分比文字
     * RN: <NativeCircleProgress showText={false} />
     */
    @ReactProp(name = "showText", defaultBoolean = true)
    fun setShowText(view: CircleProgressView, show: Boolean) {
        view.showText = show
    }
}
