package com.example.hybriddemo.base

import android.app.Application
import android.util.Log
import com.facebook.hermes.reactexecutor.HermesExecutorFactory
import com.facebook.infer.annotation.Assertions
import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.common.LifecycleState

class HYMainHost(application: Application, private val deploymentKey: String, private val rnBranch: String?) : ReactNativeHost(application) {
    init {
        // 分支热更拉取 bundle
    }

    /**
     * 是否支持调试
     * @return true 支持调试模式 ，false 不支持调试模式
     */
    override fun getUseDeveloperSupport(): Boolean {
        return true
    }

    /**
     * 获取 js bundle 文件地址，
     *
     * 当js bundle 地址不为null 的时候会加载给到js bundle文件,
     * 地址为null的时候会加载assets下getBundleAssetName() 名的js bundle文件
     *
     * @return js bundle 文件地址
     */
    override fun getJSBundleFile(): String? {
        return if (RNConfig.reactNativeHotfixOpen) {
            val codePushFilePath = try {
                if (!FeatureHotfixUtils.needFeatureHotfix(rnBranch)) {
                    Log.e("RNYuPaoMainHost", "热更")
                    CodePush.getJSBundleFile()
                } else {
                    Log.e("RNYuPaoMainHost", "分支热更")
                    FeatureHotfixUtils.getJSBundleFile(rnBranch)
                }
            } catch (e: Exception) {
                // CodePush 未初始化成功时降级使用 assets bundle
                Log.e("RNYuPaoMainHost", "getJSBundleFile 异常,降级使用 assets: ${e.message}")
                null
            }
            if (!codePushFilePath.isNullOrBlank()) codePushFilePath else null
        } else {
            null
        }
    }

    override fun getShouldRequireActivity(): Boolean {
        return false
    }

    override fun createReactInstanceManager(): ReactInstanceManager {
        val builder = ReactInstanceManager.builder().setApplication(application)
            .setJSMainModulePath(this.jsMainModuleName)
            .setUseDeveloperSupport(this.useDeveloperSupport)
            .setDevSupportManagerFactory(this.devSupportManagerFactory)
            .setRequireActivity(this.shouldRequireActivity)
            .setSurfaceDelegateFactory(this.surfaceDelegateFactory)
            .setRedBoxHandler(this.redBoxHandler)
            .setJavaScriptExecutorFactory(HermesExecutorFactory())
            .setJSIModulesPackage(this.jsiModulePackage)
            .setInitialLifecycleState(LifecycleState.BEFORE_CREATE)
//            .setDevSupportManagerFactory(SafeDevSupportManagerFactory())
            .setJSExceptionHandler(RNModuleCallExceptionHandler(application as ReactApplication))
            .setReactPackageTurboModuleManagerDelegateBuilder(this.reactPackageTurboModuleManagerDelegateBuilder)
        val var2: Iterator<*> = this.packages.iterator()

        while (var2.hasNext()) {
            val reactPackage = var2.next() as ReactPackage
            builder.addPackage(reactPackage)
        }

        val jsBundleFile = this.jsBundleFile
        if (jsBundleFile != null) {
            builder.setJSBundleFile(jsBundleFile)
        } else {
            builder.setBundleAssetName(Assertions.assertNotNull(this.bundleAssetName))
        }

        val reactInstanceManager = builder.build()
        reactInstanceManager.createReactContextInBackground()
        reactInstanceManager.packages.forEach {
//            if (it is SafeReanimatePackage) {
//                it.bindReactInstanceManager(reactInstanceManager)
//            }
        }
        return reactInstanceManager
    }

    /**
     * 获取assets js bundle 文件名
     */
    override fun getBundleAssetName(): String? {
        return RNConfig.BUNDLE_ASSET_NAME
    }

    /**
     * 获取RN 注册ReactPackage 列表
     */
    override fun getPackages(): MutableList<ReactPackage> {
        val packages = mutableListOf<ReactPackage>()

        // 热更 codePush
//        packages.add(new CodePush(
//                deploymentKey,
//            application,
//            RNCodePush.CODE_PUSH_DEBUG,
//            RNCodePush.SERVER_URL
//        ))
        // 第三方的package
//        packages.add(new FastImageViewPackage())
//        packages.add(new LottiePackage())
//        packages.add(new LinearGradientPackage())
        // RN内置 package
//        packages.add(new MainReactPackage())
        //React 自定义Package
//        packages.add(RNMainPackage())

        return packages
    }


    override fun getJSMainModuleName(): String {
        return RNConfig.JS_MAIN_MODULE_NAME
    }

    /**
     * 判断是否支持分支热更
     * @return Boolean
     */
    fun enableFeatureHotfix(): Boolean {
        return FeatureHotfixUtils.needFeatureHotfix(rnBranch)
    }
}