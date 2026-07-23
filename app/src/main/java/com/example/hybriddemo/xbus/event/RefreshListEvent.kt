package com.example.hybriddemo.xbus.event

/**
 * 列表刷新事件
 *
 * 模拟跨模块通信：编辑页/详情页操作后发出，列表页消费刷新数据
 */
data class RefreshListEvent(
    val source: String,     // 发送来源标识
    val targetId: String? = null  // 可选：指定刷新的目标 ID
)
