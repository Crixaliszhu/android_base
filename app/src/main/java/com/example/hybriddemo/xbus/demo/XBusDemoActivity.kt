package com.example.hybriddemo.xbus.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hybriddemo.xbus.XBus
import com.example.hybriddemo.xbus.event.CartUpdateEvent
import com.example.hybriddemo.xbus.event.LoginStatusEvent
import com.example.hybriddemo.xbus.event.RefreshListEvent

/**
 * XBus 跨模块通信演示 —— 主页面（事件消费者）
 *
 * 模拟"首页"角色：
 * - 监听登录状态变更事件 → 更新界面显示
 * - 监听购物车更新事件 → 更新角标
 * - 监听列表刷新事件 → 刷新数据
 *
 * 生命周期绑定：用 this（Activity LifecycleOwner）注册，
 * Activity 销毁时自动解绑，无需手动 unregister。
 */
class XBusDemoActivity : AppCompatActivity() {

    private lateinit var tvLoginStatus: TextView
    private lateinit var tvCartBadge: TextView
    private lateinit var tvEventLog: TextView

    private val eventLogs = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        // ==================== 标题 ====================
        layout.addView(createTitle("🚌 XBus 跨模块通信演示"))
        layout.addView(createDesc("本页面作为事件消费者（模拟首页模块），监听来自其他模块的事件"))

        // ==================== 状态展示区 ====================
        layout.addView(createSectionTitle("📊 实时状态"))

        tvLoginStatus = TextView(this).apply {
            text = "登录状态：未登录"
            textSize = 15f
            setPadding(0, 8, 0, 8)
        }
        layout.addView(tvLoginStatus)

        tvCartBadge = TextView(this).apply {
            text = "购物车：0 件"
            textSize = 15f
            setPadding(0, 8, 0, 16)
        }
        layout.addView(tvCartBadge)

        // ==================== 操作按钮 ====================
        layout.addView(createSectionTitle("🎯 跳转到事件生产者"))
        layout.addView(createDesc("打开其他页面（模拟其他模块），在那里触发事件，观察本页面响应"))

        layout.addView(createButton("打开「登录模块」→ 发送登录事件") {
            startActivity(Intent(this, LoginModuleActivity::class.java))
        })

        layout.addView(createButton("打开「商品模块」→ 发送购物车事件") {
            startActivity(Intent(this, ProductModuleActivity::class.java))
        })

        layout.addView(createButton("打开「编辑模块」→ 发送刷新事件") {
            startActivity(Intent(this, EditModuleActivity::class.java))
        })

        // ==================== 事件日志 ====================
        layout.addView(createSectionTitle("📋 事件日志"))
        layout.addView(createDesc("记录收到的所有事件，验证跨模块通信"))

        tvEventLog = TextView(this).apply {
            text = "暂无事件..."
            textSize = 13f
            setTextColor(0xFF333333.toInt())
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFF5F5F5.toInt())
        }
        layout.addView(tvEventLog)

        layout.addView(createButton("清空日志") {
            eventLogs.clear()
            tvEventLog.text = "暂无事件..."
        })

        scrollView.addView(layout)
        setContentView(scrollView)

        // ==================== 注册事件监听 ====================
        registerEvents()
    }

    /**
     * 注册 XBus 事件监听
     *
     * 关键点：
     * - XBus.get(this) 绑定当前 Activity 的生命周期
     * - Activity onDestroy 时自动解绑，不会内存泄漏
     * - of(Class) 指定事件类型，类型安全
     * - listen {} 注册回调
     */
    private fun registerEvents() {
        // 监听登录状态变更（来自登录模块）
        XBus.get(this).of(LoginStatusEvent::class.java).listen { event ->
            event?.let {
                val status = if (it.isLoggedIn) "已登录 (${it.userName})" else "未登录"
                tvLoginStatus.text = "登录状态：$status"
                appendLog("🔑 LoginStatusEvent: isLoggedIn=${it.isLoggedIn}, user=${it.userName}")
            }
        }

        // 监听购物车更新（来自商品模块）
        XBus.get(this).of(CartUpdateEvent::class.java).listen { event ->
            event?.let {
                tvCartBadge.text = "购物车：${it.totalCount} 件"
                appendLog("🛒 CartUpdateEvent: count=${it.totalCount}, action=${it.action}")
            }
        }

        // 监听列表刷新（来自编辑模块）
        XBus.get(this).of(RefreshListEvent::class.java).listen { event ->
            event?.let {
                appendLog("🔄 RefreshListEvent: source=${it.source}, targetId=${it.targetId}")
                Toast.makeText(this, "收到刷新通知，来自: ${it.source}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun appendLog(msg: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        eventLogs.add(0, "[$time] $msg")
        tvEventLog.text = eventLogs.joinToString("\n")
    }

    // ==================== UI 辅助 ====================

    private fun createTitle(text: String) = TextView(this).apply {
        this.text = text
        textSize = 22f
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
        textSize = 17f
        setPadding(0, 24, 0, 8)
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
