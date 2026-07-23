package com.example.hybriddemo.xbus.demo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hybriddemo.xbus.XBus
import com.example.hybriddemo.xbus.event.RefreshListEvent

/**
 * 模拟「编辑模块」—— 事件生产者
 *
 * 演示：编辑/删除操作完成后，通过 XBus 广播 RefreshListEvent，
 * 列表页收到后自动刷新，无需 startActivityForResult 等传统方式。
 *
 * 这是 recruitment_android 中最常见的 XBus 使用场景：
 * 详情页操作 → 发送事件 → 列表页刷新
 */
class EditModuleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        layout.addView(TextView(this).apply {
            text = "✏️ 编辑模块（事件生产者）"
            textSize = 20f
            setPadding(0, 0, 0, 16)
        })

        layout.addView(TextView(this).apply {
            text = "模拟编辑/删除操作后通知列表刷新，替代 onActivityResult"
            textSize = 13f
            setTextColor(0xFF999999.toInt())
            setPadding(0, 0, 0, 24)
        })

        layout.addView(createButton("模拟：编辑完成，通知列表刷新") {
            XBus.get(null).of(RefreshListEvent::class.java).post(
                RefreshListEvent(source = "编辑页", targetId = "ITEM_001")
            )
            Toast.makeText(this, "已发送刷新事件 (编辑完成)", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("模拟：删除完成，通知列表刷新") {
            XBus.get(null).of(RefreshListEvent::class.java).post(
                RefreshListEvent(source = "编辑页-删除", targetId = "ITEM_002")
            )
            Toast.makeText(this, "已发送刷新事件 (删除完成)", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("模拟：批量操作，通知全量刷新") {
            XBus.get(null).of(RefreshListEvent::class.java).post(
                RefreshListEvent(source = "编辑页-批量操作", targetId = null)
            )
            Toast.makeText(this, "已发送全量刷新事件", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("← 返回首页查看效果") {
            finish()
        })

        setContentView(layout)
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
