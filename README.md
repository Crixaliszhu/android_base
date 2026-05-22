# Android + React Native 混合开发 Demo（方式 1 + 方式 3）

## 项目概述

本 Demo 演示大型混合项目的标准集成方式：
- **方式 1（Maven 远程依赖）**：RN 核心库和三方库通过 Maven AAR 引入，原生项目不依赖 RN 源码
- **方式 3（Bundle 离线包 + 热更新）**：RN 代码打包成 bundle 放入 assets，通过热更新服务下发新版本

## 项目结构

```
HybridDemo/
├── rn_module/              # RN 独立项目（模拟独立仓库）
│   ├── src/
│   │   ├── App.tsx                    # RN 主页面
│   │   ├── pages/SecondPage.tsx       # 独立注册的 RN 页面
│   │   └── native/callRN/            # 原生调 RN 的处理器
│   ├── index.js                       # RN 入口（注册组件）
│   ├── output/                        # 打包输出目录
│   └── package.json
│
├── android_host/           # 原生宿主项目（模拟独立仓库）
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── assets/
│   │   │   │   └── index.android.bundle   # RN 打包产物（方式 3）
│   │   │   ├── java/.../
│   │   │   │   ├── MainApplication.kt     # RN 初始化
│   │   │   │   ├── HotUpdateManager.kt    # 热更新管理
│   │   │   │   ├── rn/
│   │   │   │   │   ├── RNContainerActivity.kt   # RN 页面容器
│   │   │   │   │   ├── HybridBridgeModule.kt    # RN→原生 桥接
│   │   │   │   │   ├── CallRNModule.kt          # 原生→RN 桥接
│   │   │   │   │   └── HybridBridgePackage.kt   # 模块注册
│   │   │   │   └── ui/
│   │   │   │       ├── NativeHomeActivity.kt     # 原生首页
│   │   │   │       ├── NativeDetailActivity.kt   # 原生详情页
│   │   │   │       └── NativeInputActivity.kt    # 原生输入页
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts           # Maven 依赖配置
│   └── settings.gradle.kts
│
└── README.md
```

## 全流程说明

### 第一步：RN 独立开发

```bash
cd rn_module
npm install
# 开发模式（连接 Metro 实时调试）
npx react-native start
```

### 第二步：RN 打包 Bundle（方式 3 核心）

```bash
cd rn_module
npm run bundle:android
# 输出到 rn_module/output/index.android.bundle
```

### 第三步：将 Bundle 放入原生项目

```bash
# 手动复制（开发阶段）
cp rn_module/output/index.android.bundle android_host/app/src/main/assets/

# 实际项目中由 CI/CD 自动完成：
# 1. CI 拉取 rn_module 代码
# 2. npm install && npm run bundle:android
# 3. 将 bundle 上传到热更新服务器（CodePush / 自建服务）
# 4. 或将 bundle 打入 APK 的 assets 目录
```

### 第四步：原生项目编译运行

用 Android Studio 打开 `android_host/` 目录，直接运行。
原生项目**不需要** Node.js 环境，不需要 Metro。

---

## 演示功能清单

### 1. 页面跳转

| 方向 | 实现方式 | 代码位置 |
|------|---------|---------|
| 原生 → RN | `RNContainerActivity.start(ctx, "MainApp")` | NativeHomeActivity.kt |
| 原生 → RN（带参数） | `RNContainerActivity.start(ctx, "SecondPage", bundle)` | NativeHomeActivity.kt |
| RN → 原生 | `HybridBridge.openNativePage(name, params)` | App.tsx |
| RN → 原生（等待返回值） | `HybridBridge.openNativePageForResult(name, params)` | App.tsx |
| RN → 新 RN 页面 | `HybridBridge.openRNPage(viewName, params)` | App.tsx |

### 2. 原生 ↔ RN 通信

| 方向 | 方式 | 说明 |
|------|------|------|
| 原生 → RN（事件） | `RCTDeviceEventEmitter.emit(name, params)` | 单向广播，无返回值 |
| 原生 → RN（方法调用） | `CallRNModule.callRN(module, method, params, callback)` | 有返回值 |
| RN → 原生 | `NativeModules.HybridBridge.xxx()` | Promise 返回 |

### 3. 数据持久化

- RN 和原生共享同一个 SharedPreferences（`hybrid_shared_data`）
- RN 通过 `HybridBridge.saveData/getData` 读写
- 原生直接通过 `getSharedPreferences` 读写
- 实际项目中使用 MMKV 替代（性能更好）

### 4. 热更新

```
Bundle 加载优先级：
热更新 bundle（data/files/rn_bundle/） > assets 内置 bundle

更新流程：
1. App 启动 → HotUpdateManager.getLatestBundlePath()
2. 有热更新 bundle → 加载热更新 bundle
3. 无热更新 bundle → 加载 assets/index.android.bundle
4. 后台检查更新 → 下载新 bundle → 下次启动生效
```

---

## 与实际项目（recruitment_android + yp_rn_app）的对照

| 本 Demo | 实际项目 | 说明 |
|---------|---------|------|
| `HybridBridgeModule` | `YPModule` + `YPRouterModule` + `YPStoreModule` | 实际项目按职责拆分为多个模块 |
| `CallRNModule` | `RNCallClient` + `RNCallCore` | 实际项目用反射代理模式 |
| `RNContainerActivity` | `RNBaseActivity` + `RNBaseFragment` | 实际项目支持 Activity 和 Fragment 两种容器 |
| `HybridBridgePackage` | `RNMainPackage` + `RNModuleServiceManager` | 实际项目通过 ServiceManager 解耦 |
| `HotUpdateManager` | `CodePush` + `FeatureHotfixUtils` | 实际项目支持分支热更 |
| `SharedPreferences` | `MMKV` | 实际项目使用 MMKV（性能更好） |
| 手动路由分发 | `ARouter` | 实际项目使用路由框架 |

---

## 实际项目中的 CI/CD 流程

```
┌─────────────────────────────────────────────────────────────┐
│                    RN 仓库 CI Pipeline                        │
│                                                               │
│  1. git push → 触发 CI                                       │
│  2. npm install                                               │
│  3. npm run bundle:android                                    │
│  4. 将三方 RN 库的原生代码打包为 AAR                          │
│  5. 发布 AAR 到私有 Maven 仓库                                │
│  6. 将 bundle 上传到热更新服务器                              │
│                                                               │
│  产物：                                                       │
│  • Maven AAR（react-native-svg.aar, react-native-xxx.aar）   │
│  • JS Bundle（index.android.bundle）                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  原生仓库 CI Pipeline                          │
│                                                               │
│  1. build.gradle 中声明 Maven 依赖版本号                      │
│  2. Gradle Sync 下载 AAR                                      │
│  3. 将最新 bundle 放入 assets（或依赖热更新下发）             │
│  4. 编译 APK                                                  │
│  5. 发布到应用市场                                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    热更新下发流程                              │
│                                                               │
│  1. RN 代码修改 → 打包新 bundle                              │
│  2. 上传到热更新服务器（CodePush / 自建）                     │
│  3. App 启动时检查更新                                        │
│  4. 下载新 bundle → 校验完整性                                │
│  5. 下次启动加载新 bundle（无需发版）                         │
│                                                               │
│  优势：RN 代码变更无需原生发版，审核周期从 3天 → 0天          │
└─────────────────────────────────────────────────────────────┘
```

---

## 注意事项

1. **RN 版本一致性**：`rn_module/package.json` 中的 react-native 版本必须与 `android_host/build.gradle.kts` 中的 Maven AAR 版本一致
2. **NativeModule 同步**：RN 侧调用的 NativeModule 方法必须与原生侧 `@ReactMethod` 一一对应
3. **Bundle 兼容性**：热更新的 bundle 必须与当前 App 内置的 RN 版本兼容
4. **回滚机制**：热更新 bundle 加载失败时需要回滚到 assets 内置 bundle
