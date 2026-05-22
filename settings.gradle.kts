/**
 * settings.gradle.kts
 *
 * 方式 1 + 方式 3 的核心：
 * 原生项目完全独立，不引用 RN 源码路径，不需要 node_modules。
 * 所有 RN 相关依赖通过 Maven 仓库获取。
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // React Native Maven 仓库（官方发布的 AAR）
        maven { url = uri("https://repo1.maven.org/maven2/") }
    }
}

rootProject.name = "HybridDemoHost"
include(":app")
