package com.example.hybriddemo.rn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler

/**
 * RN 容器 Activity
 *
 * 每个 RN 页面都运行在一个独立的 RNContainerActivity 中。
 * 这是参考 yp_rn_app 的 "一页一 Activity" 设计。
 *
 * 核心机制：
 * 1. 通过 Intent 传入 componentName（对应 AppRegistry 注册的组件名）
 * 2. 通过 Bundle 传入 initialProperties（RN 组件的 props）
 * 3. 创建 ReactRootView 并启动 RN 渲染
 *
 * 生命周期管理：
 * - Activity 的生命周期事件会传递给 ReactInstanceManager
 * - 确保 RN 侧能正确感知页面的显示/隐藏/销毁
 */
class RNContainerActivity : AppCompatActivity(), DefaultHardwareBackBtnHandler {

    companion object {
        private const val EXTRA_COMPONENT_NAME = "component_name"
        private const val EXTRA_INITIAL_PROPS = "initial_props"
        private const val EXTRA_INSTANCE_ID = "instance_id"

        private var instanceCounter = 0L

        /**
         * 启动 RN 容器
         *
         * @param context 上下文
         * @param componentName RN 组件名（AppRegistry.registerComponent 注册的名称）
         * @param props 传递给 RN 组件的参数
         */
        fun start(context: Context, componentName: String, props: Bundle? = null) {
            val intent = Intent(context, RNContainerActivity::class.java).apply {
                putExtra(EXTRA_COMPONENT_NAME, componentName)
                putExtra(EXTRA_INSTANCE_ID, "rn_${++instanceCounter}_${System.currentTimeMillis()}")
                props?.let { putExtra(EXTRA_INITIAL_PROPS, it) }
            }
            context.startActivity(intent)
        }

        /**
         * 启动 RN 容器并等待返回结果（接受 viewName + Bundle 参数）
         */
        fun startForResult(activity: Activity, viewName: String, props: Bundle?, requestCode: Int) {
            val intent = Intent(activity, RNContainerActivity::class.java).apply {
                putExtra(EXTRA_COMPONENT_NAME, viewName)
                putExtra(EXTRA_INSTANCE_ID, "rn_${++instanceCounter}_${System.currentTimeMillis()}")
                props?.let { putExtra(EXTRA_INITIAL_PROPS, it) }
            }
            activity.startActivityForResult(intent, requestCode)
        }
    }

    private var reactRootView: ReactRootView? = null
    private var reactInstanceManager: ReactInstanceManager? = null

    private val componentName: String
        get() = intent.getStringExtra(EXTRA_COMPONENT_NAME) ?: "MainApp"

    private val instanceId: String
        get() = intent.getStringExtra(EXTRA_INSTANCE_ID) ?: "unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取 ReactInstanceManager（全局单例）
        reactInstanceManager = (application as ReactApplication)
            .reactNativeHost
            .reactInstanceManager

        // 创建 ReactRootView
        reactRootView = ReactRootView(this)

        // 构建 initialProperties
        val launchOptions = intent.getBundleExtra(EXTRA_INITIAL_PROPS) ?: Bundle()
        launchOptions.putString("instanceId", instanceId)
        launchOptions.putString("viewName", componentName)

        // 启动 RN 渲染
        reactRootView?.startReactApplication(
            reactInstanceManager,
            componentName,
            launchOptions
        )

        setContentView(reactRootView)
    }

    override fun onPause() {
        super.onPause()
        reactInstanceManager?.onHostPause(this)
    }

    override fun onResume() {
        super.onResume()
        reactInstanceManager?.onHostResume(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        reactRootView?.unmountReactApplication()
        reactRootView = null
    }

    override fun onBackPressed() {
        reactInstanceManager?.onBackPressed()
            ?: super.onBackPressed()
    }

    /**
     * 转发 onActivityResult 给 ReactInstanceManager
     *
     * 这是 RN→RN 页面返回数据的关键：
     * 当 Activity B finish 后，Activity A 的 onActivityResult 被触发，
     * 必须转发给 ReactInstanceManager，它才会通知注册的 ActivityEventListener，
     * 从而让 HybridBridgeModule 中的监听器收到回调并 resolve Promise。
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        reactInstanceManager?.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // 开发模式下双击 R 键刷新
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            reactInstanceManager?.showDevOptionsDialog()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    /**
     * 设置返回结果并关闭
     * RN 侧通过 HybridBridge.closePageWithResult(json) 调用
     */
    fun setResultAndFinish(resultJson: String) {
        val resultIntent = Intent().apply {
            putExtra("result_data", resultJson)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
