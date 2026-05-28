package com.example.hybriddemo.base

import android.app.Application
import com.example.hybriddemo.HotUpdateManager
import com.example.hybriddemo.rn.HybridBridgePackage
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.shell.MainReactPackage
import com.facebook.soloader.SoLoader

/**
 * Application - 混合项目初始化
 *
 * 实现 ReactApplication 接口，提供 ReactNativeHost。
 *
 * 方式 3 的关键：
 * - getJSBundleFile() 返回热更新 bundle 路径（如果有）
 * - getBundleAssetName() 返回 assets 中的默认 bundle 文件名
 * - 不需要 getJSMainModuleName()（因为不连接 Metro）
 *
 * 实际项目中（参考 RNYuPaoMainHost）：
 * - 优先加载 CodePush 热更 bundle
 * - 降级加载 assets 中的内置 bundle
 */
class MainApplication : Application(), ReactApplication {

    private val mReactNativeHost: ReactNativeHost = object : ReactNativeHost(this) {

        // 是否支持调试
        override fun getUseDeveloperSupport(): Boolean = true

        override fun getPackages(): MutableList<ReactPackage> {
            return mutableListOf(
                MainReactPackage(),        // RN 内置模块
                HybridBridgePackage()      // 自定义桥接模块
            )
        }

        /**
         * JS Bundle 文件路径
         *
         * 热更新的核心：
         * - 返回 null 时加载 assets 中的 bundle（getBundleAssetName）
         * - 返回文件路径时加载该路径的 bundle（热更新下载的新 bundle）
         *
         * 实际项目中的逻辑（参考 RNYuPaoMainHost）：
         * ```kotlin
         * override fun getJSBundleFile(): String? {
         *     val codePushBundle = CodePush.getJSBundleFile()
         *     return if (!codePushBundle.isNullOrBlank()) codePushBundle else null
         * }
         * ```
         */
        override fun getJSBundleFile(): String? {
            val hotfixBundle = HotUpdateManager.getLatestBundlePath(this@MainApplication)
            return hotfixBundle  // null 则降级使用 assets bundle
        }

        /**
         * assets 中的默认 bundle 文件名
         * 对应 rn_module 打包输出的 index.android.bundle
         */
        override fun getBundleAssetName(): String = "index.android.bundle"

        /**
         * 开发模式下连接 Metro 的入口模块名
         * 仅 debug 模式有效
         */
        override fun getJSMainModuleName(): String = "index"
    }

    /**
     * 实现 ReactApplication 接口的 getReactNativeHost() 方法
     * RN 0.72 要求此方法返回 ReactNativeHost 实例
     */
    override fun getReactNativeHost(): ReactNativeHost = mReactNativeHost

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
    }
}
