package com.example.hybriddemo.base

object RNConfig {
    /**
     * js bundle 资源名称，这里是文件在assets资源文件下名称
     */
    const val BUNDLE_ASSET_NAME = "index.android.bundle"

    /**
     * js 主模块名称 ，默认为index
     */
    const val JS_MAIN_MODULE_NAME = "index"

    /**
     * rn 主页模块名称
     */
    const val MAIN_COMPONENT_NAME = "yp_rn_app"

    /**
     * 跳转RN页面传参键名
     */
    const val PAGER_VIEW_NAME = "viewName"

    /**
     * RN 页面传参，token键名， 对应移动端 singletoken
     */
    const val PAGER_DATA_TOKEN_KEY = "token"
    /**
     * 是否开启热更
     */
    val reactNativeHotfixOpen by lazy {
        // 线上强制开启热更，测试环境则可以随时修改是否开启热更
        true
    }
}