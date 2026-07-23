package com.example.hybriddemo.xbus.demo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hybriddemo.xbus.XBus
import com.example.hybriddemo.xbus.event.LoginStatusEvent

/**
 * 模拟「登录模块」—— 事件生产者
 *
 * 演示：登录/登出操作后，通过 XBus 广播 LoginStatusEvent，
 * 首页模块无需直接依赖登录模块即可感知状态变更。
 *
 * 发送端使用 XBus.get(null)：
 * - 发送不需要绑定生命周期
 * - null 表示"全局发送，不关心订阅者生命周期"
 */
class LoginModuleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        layout.addView(TextView(this).apply {
            text = "🔐 登录模块（事件生产者）"
            textSize = 20f
            setPadding(0, 0, 0, 16)
        })

        layout.addView(TextView(this).apply {
            text = "点击按钮模拟登录/登出，事件将通过 XBus 广播到首页"
            textSize = 13f
            setTextColor(0xFF999999.toInt())
            setPadding(0, 0, 0, 24)
        })

        layout.addView(createButton("模拟登录成功") {
            // 发送登录成功事件
            XBus.get(null).of(LoginStatusEvent::class.java).post(
                LoginStatusEvent(
                    isLoggedIn = true,
                    userId = "U_10086",
                    userName = "张三"
                )
            )
            Toast.makeText(this, "已发送登录事件", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("模拟切换账号") {
            XBus.get(null).of(LoginStatusEvent::class.java).post(
                LoginStatusEvent(
                    isLoggedIn = true,
                    userId = "U_10087",
                    userName = "李四"
                )
            )
            Toast.makeText(this, "已发送切换账号事件", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("模拟退出登录") {
            XBus.get(null).of(LoginStatusEvent::class.java).post(
                LoginStatusEvent(isLoggedIn = false)
            )
            Toast.makeText(this, "已发送退出登录事件", Toast.LENGTH_SHORT).show()
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
