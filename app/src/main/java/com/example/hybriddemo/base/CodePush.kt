package com.example.hybriddemo.base

/**
 * 引入方式说明
 * com.microsoft.codepush.react 这个包不是从 Maven Central 或 Google Maven 获取的，它的来源链路是：
 *
 * RN 项目 node_modules/react-native-code-push/android/
 *     ↓ 包含 com.microsoft.codepush.react 包下的 Java 源码
 *     ↓ CI 提取并编译为 AAR
 *     ↓ 发布到私有 Maven（repository.vrtbbs.com）
 *     ↓ 坐标：com.yupao.react:yp-react-native-code-push:1.0.0
 *     ↓
 * 原生项目通过 Gradle 依赖引入
 * 如果你要在新项目中引入 CodePush
 * 有两种方式：
 *
 * 方式 A：源码集成（小项目/Demo）
 * // settings.gradle
 * include ':react-native-code-push'
 * project(':react-native-code-push').projectDir =
 *     new File(rootDir, '../node_modules/react-native-code-push/android/app')
 *
 * // app/build.gradle
 * implementation project(':react-native-code-push')
 * 方式 B：打包为 AAR 发布到私有 Maven（大型项目，你们的做法）
 * # 1. 从 node_modules 中提取 CodePush 的 android 源码
 * cd node_modules/react-native-code-push/android
 *
 * # 2. 编译为 AAR（通过 Gradle 或 CI 脚本）
 * ./gradlew assembleRelease
 * # 产物：build/outputs/aar/react-native-code-push-release.aar
 *
 * # 3. 发布到私有 Maven
 * # 使用 maven-publish 插件上传到 repository.vrtbbs.com
 * # 坐标：com.yupao.react:yp-react-native-code-push:1.0.0
 * 方式 C：直接使用 npm 包的 Maven 发布（如果三方库自己发布了）
 * 部分 RN 三方库会自己发布到 Maven Central，但 react-native-code-push 没有官方 Maven 发布，所以必须自己打包。
 *
 * 总结
 * com.microsoft.codepush.react 这个 Java 包名是 CodePush SDK 源码中定义的，无论用哪种方式引入，最终编译后的类路径都是 com.microsoft.codepush.react.CodePush。区别只是源码从哪里来
 */
object CodePush {
    fun getJSBundleFile(): String{
        return ""
    }
}