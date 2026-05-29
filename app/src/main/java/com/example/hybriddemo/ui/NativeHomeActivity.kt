package com.example.hybriddemo.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.example.hybriddemo.rn.CallRNModule
import com.example.hybriddemo.rn.HybridBridgeModule
import com.example.hybriddemo.rn.RNContainerActivity
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * 原生首页
 *
 * 演示：
 * 1. 原生 → RN 页面跳转
 * 2. 原生 → RN 事件发送
 * 3. 原生调用 RN 方法（获取返回值）
 * 4. 原生读写共享数据
 */
class NativeHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        // ==================== 标题 ====================
        layout.addView(createTitle("🏠 原生首页"))
        layout.addView(createDesc("这是原生 Activity，演示原生侧的各种操作"))

        // ==================== 页面跳转 ====================
        layout.addView(createSectionTitle("📱 页面跳转"))

        layout.addView(createButton("打开 RN 主应用") {
            RNContainerActivity.start(this, "HomePage")
        })

        layout.addView(createButton("打开 RN 独立页面（带参数）") {
            RNContainerActivity.start(
                this,
                "SecondPage",
                bundleOf(
                    "title" to "从原生传入的标题",
                    "itemId" to "ITEM_001"
                )
            )
        })

        // ==================== 原生 → RN 事件 ====================
        layout.addView(createSectionTitle("📡 原生 → RN 事件"))
        layout.addView(createDesc("通过 RCTDeviceEventEmitter 发送事件，RN 侧监听"))

        layout.addView(createButton("发送登录事件给 RN") {
            sendEventToRN("onLoginStateChanged", WritableNativeMap().apply {
                putBoolean("isLoggedIn", true)
                putString("userId", "USER_12345")
            })
        })

        layout.addView(createButton("发送刷新通知给 RN") {
            sendEventToRN("onRefreshData", WritableNativeMap().apply {
                putString("reason", "原生数据已更新")
                putDouble("timestamp", System.currentTimeMillis().toDouble())
            })
        })

        // ==================== 原生调用 RN 方法 ====================
        layout.addView(createSectionTitle("🔄 原生调用 RN 方法"))
        layout.addView(createDesc("原生主动调用 RN 逻辑并获取返回值"))

        layout.addView(createButton("调用 RN: math.add(3, 5)") {
            callRNMethod("math", "add", mapOf("a" to 3, "b" to 5))
        })

        layout.addView(createButton("调用 RN: math.factorial(10)") {
            callRNMethod("math", "factorial", mapOf("n" to 10))
        })

        layout.addView(createButton("调用 RN: data.formatPrice(12345)") {
            callRNMethod("data", "formatPrice", mapOf("price" to 12345, "currency" to "¥"))
        })

        layout.addView(createButton("调用 RN: data.getVersion()") {
            callRNMethod("data", "getVersion", null)
        })

        // ==================== 数据共享 ====================
        layout.addView(createSectionTitle("💾 数据共享"))
        layout.addView(createDesc("原生和 RN 共享同一个 SharedPreferences"))

        layout.addView(createButton("原生写入共享数据") {
            val prefs = getSharedPreferences(HybridBridgeModule.SHARED_PREFS_NAME, 0)
            prefs.edit()
                .putString("native_data", "原生写入_${System.currentTimeMillis()}")
                .putString("user_token", "token_abc123")
                .apply()
            Toast.makeText(this, "已写入 SharedPreferences", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("原生读取共享数据") {
            val prefs = getSharedPreferences(HybridBridgeModule.SHARED_PREFS_NAME, 0)
            val all = prefs.all
            val msg = if (all.isEmpty()) "数据为空" else all.entries.joinToString("\n") { "${it.key} = ${it.value}" }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        })

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    /** 向 RN 发送事件 */
    private fun sendEventToRN(eventName: String, params: WritableNativeMap) {
        try {
            val reactContext = (application as? ReactApplication)
                ?.reactNativeHost?.reactInstanceManager?.currentReactContext

            if (reactContext != null) {
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit(eventName, params)
                Toast.makeText(this, "事件已发送: $eventName", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "RN 未初始化，请先打开 RN 页面", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "发送失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** 调用 RN 方法 */
    private fun callRNMethod(moduleName: String, methodName: String, params: Map<String, Any>?) {
        val reactContext = (application as? ReactApplication)
            ?.reactNativeHost?.reactInstanceManager?.currentReactContext

        if (reactContext == null) {
            Toast.makeText(this, "RN 未初始化，请先打开 RN 页面", Toast.LENGTH_SHORT).show()
            return
        }

        CallRNModule.callRN(
            reactContext as ReactApplicationContext,
            moduleName,
            methodName,
            params
        ) { result ->
            runOnUiThread {
                Toast.makeText(
                    this,
                    "RN 返回: ${result ?: "调用失败"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ==================== UI 辅助方法 ====================

    private fun createTitle(text: String) = TextView(this).apply {
        this.text = text
        textSize = 24f
        setPadding(0, 0, 0, 8)
    }

    private fun createDesc(text: String) = TextView(this).apply {
        this.text = text
        textSize = 13f
        setTextColor(0xFF999999.toInt())
        setPadding(0, 0, 0, 16)
    }

    private fun createSectionTitle(text: String) = TextView(this).apply {
        this.text = text
        textSize = 18f
        setPadding(0, 32, 0, 12)
    }

    private fun createButton(text: String, onClick: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 12 }
    }
}
