package com.example.hybriddemo.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 原生详情页
 *
 * 演示 RN → 原生页面跳转，接收 RN 传递的参数。
 * RN 侧调用：HybridBridge.openNativePage('NativeDetailPage', { title: '...', id: '...' })
 */
class NativeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("title") ?: "无标题"
        val id = intent.getStringExtra("id") ?: "无ID"
        val timestamp = intent.getStringExtra("timestamp") ?: ""

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        layout.addView(TextView(this).apply {
            text = "📄 原生详情页"
            textSize = 24f
            setPadding(0, 0, 0, 24)
        })

        layout.addView(TextView(this).apply {
            text = "接收到 RN 传递的参数："
            textSize = 16f
            setPadding(0, 0, 0, 12)
        })

        layout.addView(TextView(this).apply {
            text = "• title: $title\n• id: $id\n• timestamp: $timestamp"
            textSize = 14f
            setPadding(16, 0, 0, 24)
        })

        layout.addView(TextView(this).apply {
            text = "💡 通信流程：\n" +
                "RN: HybridBridge.openNativePage('NativeDetailPage', params)\n" +
                "  ↓\n" +
                "原生: HybridBridgeModule.openNativePage()\n" +
                "  ↓\n" +
                "原生: startActivity(NativeDetailActivity)\n" +
                "  ↓\n" +
                "原生: intent.getStringExtra() 获取参数"
            textSize = 12f
            setPadding(0, 0, 0, 32)
            setTextColor(0xFF666666.toInt())
        })

        layout.addView(Button(this).apply {
            text = "返回"
            isAllCaps = false
            setOnClickListener { finish() }
        })

        setContentView(layout)
    }
}
