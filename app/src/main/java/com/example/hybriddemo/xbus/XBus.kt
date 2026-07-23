package com.example.hybriddemo.xbus

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * XBus —— 轻量级、生命周期感知的事件总线
 *
 * 复刻鱼泡项目 com.yupao.utils.event.XBus 的核心设计：
 * - 链式 API：XBus.get(owner).of(Event::class.java).listen { }
 * - 发送：XBus.get(null).of(Event::class.java).post(event)
 * - 生命周期自动解绑：传入 LifecycleOwner 时，DESTROYED 自动移除订阅
 *
 * 设计原理：
 * 1. 全局单例持有 Map<Class<*>, List<Subscription>>，按事件类型索引订阅者
 * 2. get(owner) 返回 BusAccessor，用于后续 of/listen/post
 * 3. of(Class<T>) 返回 EventStream<T>，锁定事件类型
 * 4. listen {} 注册回调，并绑定 LifecycleOwner（如有）实现自动解绑
 * 5. post(event) 遍历该类型所有活跃订阅者，主线程分发
 */
object XBus {

    /** 事件类型 -> 订阅者列表 */
    private val subscriptions = ConcurrentHashMap<Class<*>, CopyOnWriteArrayList<Subscription<*>>>()

    /**
     * 获取总线访问器
     * @param owner 生命周期宿主（Activity/Fragment/viewLifecycleOwner）
     *              传 null 表示不绑定生命周期（通常用于发送端）
     */
    fun get(owner: LifecycleOwner?): BusAccessor = BusAccessor(owner)

    /** 内部：注册订阅 */
    internal fun <T : Any> register(eventType: Class<T>, subscription: Subscription<T>) {
        val list = subscriptions.getOrPut(eventType) { CopyOnWriteArrayList() }
        list.add(subscription)
    }

    /** 内部：移除订阅 */
    internal fun <T : Any> unregister(eventType: Class<T>, subscription: Subscription<T>) {
        subscriptions[eventType]?.remove(subscription)
    }

    /** 内部：移除某个 owner 的所有订阅 */
    internal fun unregisterAll(owner: LifecycleOwner) {
        subscriptions.values.forEach { list ->
            list.removeAll { it.owner == owner }
        }
    }

    /** 内部：发布事件 */
    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> dispatch(eventType: Class<T>, event: T) {
        val list = subscriptions[eventType] ?: return
        for (sub in list) {
            (sub as Subscription<T>).callback(event)
        }
    }
}

/**
 * 总线访问器 —— 绑定了 LifecycleOwner 的中间对象
 */
class BusAccessor(internal val owner: LifecycleOwner?) {
    /**
     * 指定事件类型，返回 EventStream
     */
    fun <T : Any> of(eventType: Class<T>): EventStream<T> = EventStream(this, eventType)
}

/**
 * 事件流 —— 锁定了 owner + eventType，提供 listen/post 操作
 */
class EventStream<T : Any>(
    private val accessor: BusAccessor,
    private val eventType: Class<T>
) {

    /**
     * 订阅事件
     * 如果 BusAccessor 持有 LifecycleOwner，则在 DESTROYED 时自动解绑
     */
    fun listen(callback: (T?) -> Unit) {
        val subscription = Subscription(accessor.owner, callback)
        XBus.register(eventType, subscription)

        // 绑定生命周期，自动解绑
        accessor.owner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                XBus.unregister(eventType, subscription)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    /**
     * 发布事件（广播给所有该类型的订阅者）
     */
    fun post(event: T) {
        XBus.dispatch(eventType, event)
    }
}

/**
 * 订阅记录
 */
data class Subscription<T : Any>(
    val owner: LifecycleOwner?,
    val callback: (T?) -> Unit
)
