package com.example.hybriddemo

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * 热更新管理器
 *
 * 模拟 CodePush 的核心逻辑：
 * 1. 检查服务端是否有新版本 bundle
 * 2. 下载新 bundle 到本地
 * 3. 下次启动时加载新 bundle
 *
 * 实际项目中（参考 recruitment_android 的 CodePush 集成）：
 * - 使用 react-native-code-push 库
 * - 或自建热更新服务（更灵活，支持分支热更、灰度发布）
 *
 * Bundle 加载优先级：
 * 热更新 bundle（data/files/rn_bundle/） > assets 内置 bundle
 */
object HotUpdateManager {

    private const val PREFS_NAME = "rn_hot_update"
    private const val KEY_BUNDLE_VERSION = "bundle_version"
    private const val KEY_BUNDLE_PATH = "bundle_path"
    private const val KEY_UPDATE_TIME = "update_time"
    private const val BUNDLE_DIR = "rn_bundle"
    private const val BUNDLE_FILE = "index.android.bundle"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 获取最新的 bundle 路径
     *
     * @return 热更新 bundle 路径，如果没有则返回 null（使用 assets 内置 bundle）
     */
    fun getLatestBundlePath(context: Context): String? {
        val prefs = getPrefs(context)
        val bundlePath = prefs.getString(KEY_BUNDLE_PATH, null) ?: return null
        val file = File(bundlePath)
        return if (file.exists()) bundlePath else null
    }

    /**
     * 获取当前 bundle 信息
     */
    fun getBundleInfo(context: Context): BundleInfo {
        val prefs = getPrefs(context)
        val bundlePath = prefs.getString(KEY_BUNDLE_PATH, null)
        val version = prefs.getString(KEY_BUNDLE_VERSION, "1.0.0") ?: "1.0.0"
        val updateTime = prefs.getLong(KEY_UPDATE_TIME, 0)

        val source = if (bundlePath != null && File(bundlePath).exists()) {
            "热更新"
        } else {
            "内置 assets"
        }

        return BundleInfo(
            version = version,
            source = source,
            updateTime = if (updateTime > 0) {
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date(updateTime))
            } else {
                null
            }
        )
    }

    /**
     * 模拟检查更新
     *
     * 实际项目中这里会：
     * 1. 请求服务端 API 获取最新版本信息
     * 2. 对比本地版本号
     * 3. 如果有新版本，下载 bundle 文件
     * 4. 校验 bundle 完整性（MD5/SHA256）
     * 5. 保存到本地，下次启动生效
     */
    fun checkUpdate(context: Context): UpdateResult {
        // 模拟：当前已是最新版本
        return UpdateResult(
            hasUpdate = false,
            message = "当前已是最新版本 (v${getBundleInfo(context).version})\n\n" +
                "实际项目中此处会：\n" +
                "1. 请求热更新服务器\n" +
                "2. 对比版本号\n" +
                "3. 下载新 bundle\n" +
                "4. 校验完整性\n" +
                "5. 下次启动生效"
        )
    }

    /**
     * 模拟应用热更新 bundle
     *
     * 实际项目中的流程：
     * 1. 从服务器下载新 bundle zip
     * 2. 解压到 BUNDLE_DIR
     * 3. 校验 bundle 文件
     * 4. 更新 SharedPreferences 中的版本信息
     * 5. 重启 RN 运行时（或下次冷启动生效）
     */
    fun applyUpdate(context: Context, bundleBytes: ByteArray, version: String) {
        val bundleDir = File(context.filesDir, BUNDLE_DIR)
        if (!bundleDir.exists()) bundleDir.mkdirs()

        val bundleFile = File(bundleDir, BUNDLE_FILE)
        bundleFile.writeBytes(bundleBytes)

        getPrefs(context).edit()
            .putString(KEY_BUNDLE_PATH, bundleFile.absolutePath)
            .putString(KEY_BUNDLE_VERSION, version)
            .putLong(KEY_UPDATE_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * 回滚到内置 bundle
     * 当热更新 bundle 加载失败时调用
     */
    fun rollback(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_BUNDLE_PATH)
            .apply()
    }

    data class BundleInfo(
        val version: String,
        val source: String,
        val updateTime: String?
    )

    data class UpdateResult(
        val hasUpdate: Boolean,
        val message: String
    )
}
