# Proposal: add-bangumi-detail-bottom-sheet

## 概述

在播放页 `BangumiDetailPreview` 中的 MoreHoriz 按钮目前点击后是提示"开发中"。改为使用 `ModalBottomSheet` 弹出详情页，提供完整的番剧信息查看功能。

## 目标

1. 将 MoreHoriz 按钮的点击行为从显示 TODO 提示改为弹出 BottomSheet 详情页
2. BottomSheet 中复用现有的 `BangumiDetailVM` 和详情页组件
3. BottomSheet 模式下：
   - 顶部背景不显示模糊图片，只用纯色背景
   - 不显示大播放按钮，收藏按钮移到原来播放按钮的位置（中间）
   - 剧集不可交互（不显示播放图标，不响应点击）
   - 保留 Tab 切换（详情 / 剧集 / 评论）

## 范围

### 修改文件

1. **BangumiDetailVM.kt** - 确保 panelMode 参数被正确传递
2. **BangumiDetailHeader.kt** - 添加 showPlayBtn 参数，控制播放按钮显示和收藏按钮位置
3. **BangumiDetailSubPage.kt** - 添加 interactive 参数，控制剧集交互
4. **新建 BangumiDetailPanel.kt** - BottomSheet 专用详情页组件
5. **BangumiMediaPopup.kt** - 实现 ModalBottomSheet，调用 BangumiDetailPanel
6. **BangumiMediaCommonVM.kt** - 移除 easyTODO 调用

### 不在范围内

- 不修改现有的详情页路由
- 不修改 BangumiDetailVM 的数据加载逻辑
- 不添加新的 API 调用

## 用户价值

- 用户可以快速查看番剧详情，无需跳转到详情页
- 提供更流畅的交互体验，保持在播放页上下文中

## 风险

- 低风险：主要是 UI 层面的修改，复用现有组件
- 需要确保 BottomSheet 的滚动行为与详情页组件兼容
