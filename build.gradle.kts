/**
 * 项目级 build.gradle.kts
 *
 * 注意：不需要任何 RN 相关的 Gradle 插件。
 * RN 的原生代码全部通过 Maven AAR 引入。
 */
plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}
