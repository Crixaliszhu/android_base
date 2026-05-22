/**
 * app 模块 build.gradle.kts
 *
 * 方式 1（Maven 远程依赖）的核心配置：
 * - react-android: RN 核心框架 AAR
 * - hermes-android: Hermes JS 引擎 AAR
 *
 * 实际项目中还会有：
 * - 三方 RN 库的 AAR（如 react-native-svg、react-native-reanimated 等）
 * - 这些 AAR 由 CI 从 node_modules 中提取并发布到私有 Maven
 *
 * 方式 3（Bundle 离线包）：
 * - JS Bundle 文件放在 app/src/main/assets/index.android.bundle
 * - 通过 CodePush 或自建热更新服务下发新 bundle
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.hybriddemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hybriddemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // 仅支持 arm 架构（RN 的 so 库限制）
    defaultConfig {
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }
}

dependencies {
    // ==================== 方式 1：Maven 远程依赖 ====================
    // RN 核心框架（对应 yupaoLibs.yupao.react.native.base）
    implementation("com.facebook.react:react-android:0.72.5")
    // Hermes JS 引擎（启动更快、内存更低）
    implementation("com.facebook.react:hermes-android:0.72.5")

    // ==================== 实际项目中还会有这些 AAR ====================
    // implementation("com.your.company:react-native-svg:x.x.x")
    // implementation("com.your.company:react-native-reanimated:x.x.x")
    // implementation("com.your.company:react-native-code-push:x.x.x")
    // implementation("com.your.company:react-native-fast-image:x.x.x")
    // 这些 AAR 由 CI 从 RN 项目的 node_modules 中提取并发布到私有 Maven

    // ==================== Android 基础依赖 ====================
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
