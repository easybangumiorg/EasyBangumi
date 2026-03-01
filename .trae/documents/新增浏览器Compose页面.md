## 实现计划

### 1. 创建 BrowserPage 参数类
- 在 `shared/src/commonMain/kotlin/org/easybangumi/next/shared/compose/browser/` 创建 `BrowserPageParam.kt`
- 包含 url 参数和可选的配置参数

### 2. 创建 BrowserPage Compose 接口
- 在 `shared/src/commonMain/kotlin/org/easybangumi/next/shared/compose/browser/BrowserPage.kt` 创建 expect 函数
- 定义统一的浏览器页面接口

### 3. 实现 Desktop 平台浏览器页面
- 在 `shared/src/desktopMain/kotlin/org/easybangumi/next/shared/compose/browser/BrowserPage.desktop.kt` 创建实际实现
- 使用 JCEF 集成，包含地址栏、导航按钮、加载状态等
- 支持页面加载、前进后退、刷新等功能

### 4. 实现 Android 平台浏览器页面
- 在 `shared/src/androidMain/kotlin/org/easybangumi/next/shared/compose/browser/BrowserPage.android.kt` 创建实际实现
- 使用 Android WebView 集成，包含地址栏、导航按钮、加载状态等
- 支持页面加载、前进后退、刷新等功能

### 5. 在 Router 中添加路由
- 修改 `Router.kt`，添加 `BrowserPage` 路由类
- 在 `NavHost` 中添加 composable 路由配置

### 6. 添加导航方法
- 在 `Router.kt` 中添加 `navigateToBrowser` 扩展函数
- 支持从任何地方跳转到浏览器页面

### 7. 平台特化实现特性
- Desktop: 使用 JCEF 提供完整的浏览器功能
- Android: 使用系统 WebView 提供原生体验
- 统一的参数接口和导航体验

这个实现将提供一个功能完整的浏览器页面，支持 URL 输入、页面导航、加载状态显示等核心功能，同时保持跨平台的一致性。