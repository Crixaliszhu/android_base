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
 * RN 容器 Activity - 单组件模式
 *
 * 所有 RN 页面都通过同一个组件名 "HybridDemoRN" 加载，
 * 通过 initialProperties 中的 viewName 参数告诉 RN 侧渲染哪个页面。
 *
 * 原生侧只需要知道 viewName（页面标识），不需要知道 RN 组件注册细节。
 */
class RNContainerActivity : AppCompatActivity(), DefaultHardwareBackBtnHandler {

    companion object {
        private const val EXTRA_INITIAL_PROPS = "initial_props"
        private const val EXTRA_INSTANCE_ID = "instance_id"

        /** 单组件模式：所有 RN 页面共用同一个组件名 */
        private const val RN_COMPONENT_NAME = "HybridDemoRN"

        private var instanceCounter = 0L

        /**
         * 启动 RN 容器
         *
         * @param context 上下文
         * @param viewName RN 页面标识（传入 props.viewName，由 RN 侧路由）
         * @param props 传递给 RN 页面的业务参数
         */
        fun start(context: Context, viewName: String, props: Bundle? = null) {
            val intent = Intent(context, RNContainerActivity::class.java).apply {
                putExtra(EXTRA_INSTANCE_ID, "rn_${++instanceCounter}_${System.currentTimeMillis()}")
                val mergedProps = (props ?: Bundle()).apply {
                    putString("viewName", viewName)
                }
                putExtra(EXTRA_INITIAL_PROPS, mergedProps)
            }
            context.startActivity(intent)
        }

        /**
         * 启动 RN 容器并等待返回结果
         */
        fun startForResult(activity: Activity, viewName: String, props: Bundle? = null, requestCode: Int) {
            val intent = Intent(activity, RNContainerActivity::class.java).apply {
                putExtra(EXTRA_INSTANCE_ID, "rn_${++instanceCounter}_${System.currentTimeMillis()}")
                val mergedProps = (props ?: Bundle()).apply {
                    putString("viewName", viewName)
                }
                putExtra(EXTRA_INITIAL_PROPS, mergedProps)
            }
            activity.startActivityForResult(intent, requestCode)
        }
    }

    private var reactRootView: ReactRootView? = null
    private var reactInstanceManager: ReactInstanceManager? = null

    private val instanceId: String
        get() = intent.getStringExtra(EXTRA_INSTANCE_ID) ?: "unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reactInstanceManager = (application as ReactApplication)
            .reactNativeHost
            .reactInstanceManager

        reactRootView = ReactRootView(this)

        // 构建 initialProperties（包含 viewName + instanceId + 业务参数）
        val launchOptions = intent.getBundleExtra(EXTRA_INITIAL_PROPS) ?: Bundle()
        launchOptions.putString("instanceId", instanceId)

        // 始终使用同一个组件名，RN 侧根据 viewName 路由
        reactRootView?.startReactApplication(
            reactInstanceManager,
            RN_COMPONENT_NAME,
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

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }

    /**
     * 转发 onActivityResult 给 ReactInstanceManager
     * 确保 HybridBridgeModule 中的 ActivityEventListener 能收到回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        reactInstanceManager?.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
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
