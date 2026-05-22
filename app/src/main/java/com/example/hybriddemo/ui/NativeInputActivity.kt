package com.example.hybriddemo.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 原生输入页（带返回值）
 *
 * 演示 RN 跳转到原生页面并等待返回结果。
 * 类似 Android 的 startActivityForResult 模式。
 *
 * 流程：
 * 1. RN: await HybridBridge.openNativePageForResult('NativeInputPage', {hint: '...'})
 * 2. 原生: 打开此页面，用户输入
 * 3. 原生: setResult(RESULT_OK, intent) + finish()
 * 4. 原生: HybridBridgeModule 的 onActivityResult 接收结果
 * 5. 原生: promise.resolve(result) 返回给 RN
 */
class NativeInputActivity : AppCompatActivity() {

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hint = intent.getStringExtra("hint") ?: "请输入内容"

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        layout.addView(TextView(this).apply {
            text = "✏️ 原生输入页"
            textSize = 24f
            setPadding(0, 0, 0, 16)
        })

        layout.addView(TextView(this).apply {
            text = "RN 跳转到此页面并等待返回结果"
            textSize = 14f
            setTextColor(0xFF666666.toInt())
            setPadding(0, 0, 0, 24)
        })

        editText = EditText(this).apply {
            this.hint = hint
            textSize = 16f
            setPadding(24, 24, 24, 24)
        }
        layout.addView(editText)

        layout.addView(TextView(this).apply { text = ""; textSize = 8f })

        layout.addView(Button(this).apply {
            text = "确认并返回数据给 RN"
            isAllCaps = false
            setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra("result_input", editText.text.toString())
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        })

        layout.addView(Button(this).apply {
            text = "取消"
            isAllCaps = false
            setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })

        layout.addView(TextView(this).apply {
            text = "\n💡 RN 侧代码：\n" +
                "const result = await HybridBridge.openNativePageForResult(\n" +
                "  'NativeInputPage', {hint: '请输入'}\n" +
                ");\n" +
                "// result = { input: '用户输入', code: -1 }"
            textSize = 12f
            setTextColor(0xFF666666.toInt())
            setPadding(0, 16, 0, 0)
        })

        setContentView(layout)
    }
}
