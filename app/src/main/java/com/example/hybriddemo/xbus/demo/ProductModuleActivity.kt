package com.example.hybriddemo.xbus.demo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hybriddemo.xbus.XBus
import com.example.hybriddemo.xbus.event.CartUpdateEvent

/**
 * 模拟「商品模块」—— 事件生产者
 *
 * 演示：加购/减购/清空购物车后，通过 XBus 广播 CartUpdateEvent，
 * 首页的购物车角标实时更新，无需回调或接口耦合。
 */
class ProductModuleActivity : AppCompatActivity() {

    private var cartCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        layout.addView(TextView(this).apply {
            text = "🛍️ 商品模块（事件生产者）"
            textSize = 20f
            setPadding(0, 0, 0, 16)
        })

        layout.addView(TextView(this).apply {
            text = "模拟加购/减购操作，首页购物车角标会实时响应"
            textSize = 13f
            setTextColor(0xFF999999.toInt())
            setPadding(0, 0, 0, 24)
        })

        layout.addView(createButton("加入购物车 +1") {
            cartCount++
            XBus.get(null).of(CartUpdateEvent::class.java).post(
                CartUpdateEvent(totalCount = cartCount, action = "add")
            )
            Toast.makeText(this, "已加购，当前 $cartCount 件", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("加入购物车 +5") {
            cartCount += 5
            XBus.get(null).of(CartUpdateEvent::class.java).post(
                CartUpdateEvent(totalCount = cartCount, action = "add")
            )
            Toast.makeText(this, "已加购，当前 $cartCount 件", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("移除商品 -1") {
            if (cartCount > 0) cartCount--
            XBus.get(null).of(CartUpdateEvent::class.java).post(
                CartUpdateEvent(totalCount = cartCount, action = "remove")
            )
            Toast.makeText(this, "已移除，当前 $cartCount 件", Toast.LENGTH_SHORT).show()
        })

        layout.addView(createButton("清空购物车") {
            cartCount = 0
            XBus.get(null).of(CartUpdateEvent::class.java).post(
                CartUpdateEvent(totalCount = 0, action = "clear")
            )
            Toast.makeText(this, "已清空购物车", Toast.LENGTH_SHORT).show()
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
