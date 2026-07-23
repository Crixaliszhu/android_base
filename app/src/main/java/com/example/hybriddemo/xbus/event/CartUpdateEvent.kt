package com.example.hybriddemo.xbus.event

/**
 * 购物车更新事件
 *
 * 模拟跨模块通信：商品详情页/购物车页发出，底部Tab角标/购物车列表消费
 */
data class CartUpdateEvent(
    val totalCount: Int,
    val action: String // "add" | "remove" | "clear"
)
