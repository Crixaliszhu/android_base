package com.example.hybriddemo.rn.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * 原生圆形进度条 View
 *
 * 演示原生 View 暴露给 RN 使用的场景。
 * 实际项目中可能是：地图、视频播放器、图表、动画组件等高性能原生 View。
 *
 * RN 侧通过 requireNativeComponent 使用此 View，
 * 并通过 props 控制进度、颜色等属性。
 */
class CircleProgressView(context: Context) : View(context) {

    /** 当前进度 0~100 */
    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            invalidate()
        }

    /** 进度条颜色 */
    var progressColor: Int = Color.parseColor("#007AFF")
        set(value) {
            field = value
            progressPaint.color = value
            invalidate()
        }

    /** 轨道颜色 */
    var trackColor: Int = Color.parseColor("#E0E0E0")
        set(value) {
            field = value
            trackPaint.color = value
            invalidate()
        }

    /** 进度条宽度 (px) */
    var strokeWidth: Float = 12f
        set(value) {
            field = value
            progressPaint.strokeWidth = value
            trackPaint.strokeWidth = value
            invalidate()
        }

    /** 是否显示中间文字 */
    var showText: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CircleProgressView.strokeWidth
        strokeCap = Paint.Cap.ROUND
        color = progressColor
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CircleProgressView.strokeWidth
        color = trackColor
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#333333")
    }

    private val rectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) - strokeWidth

        // 绘制轨道
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rectF, 0f, 360f, false, trackPaint)

        // 绘制进度
        val sweepAngle = progress / 100f * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)

        // 绘制中间文字
        if (showText) {
            textPaint.textSize = radius * 0.4f
            val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText("${progress}%", centerX, textY, textPaint)
        }
    }
}
