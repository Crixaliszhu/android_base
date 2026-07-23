package com.example.hybriddemo.xbus.event

/**
 * 登录状态变更事件
 *
 * 模拟跨模块通信：登录模块发出，首页/个人中心/消息模块消费
 */
data class LoginStatusEvent(
    val isLoggedIn: Boolean,
    val userId: String? = null,
    val userName: String? = null
)
