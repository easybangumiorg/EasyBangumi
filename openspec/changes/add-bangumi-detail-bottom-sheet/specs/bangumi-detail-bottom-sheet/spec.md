# Spec: bangumi-detail-bottom-sheet

## 概述

在播放页 BangumiDetailPreview 中的 MoreHoriz 按钮改为使用 bottomSheet 弹出详情页。

## 功能需求

### FR-1: BottomSheet 弹出

**描述**: 点击 MoreHoriz 按钮时，弹出 ModalBottomSheet 显示番剧详情。

**验收标准**:
- [x] 点击 MoreHoriz 按钮弹出 BottomSheet
- [x] BottomSheet 显示番剧详情信息
- [x] 下滑或点击遮罩可关闭 BottomSheet

### FR-2: 顶部背景样式

**描述**: BottomSheet 模式下，顶部背景不显示模糊图片，只用纯色背景。

**验收标准**:
- [x] 不显示背景模糊图片
- [x] 使用纯色背景
- [x] 背景色与主题一致

### FR-3: 播放按钮隐藏

**描述**: BottomSheet 模式下，不显示大播放按钮，收藏按钮移到原来播放按钮的位置。

**验收标准**:
- [x] 不显示播放按钮
- [x] 收藏按钮移到中间位置
- [x] 收藏按钮功能正常

### FR-4: 剧集交互禁用

**描述**: BottomSheet 模式下，剧集列表不可交互。

**验收标准**:
- [x] 不显示播放图标
- [x] 点击剧集无响应
- [x] 剧集信息正常显示

### FR-5: Tab 切换

**描述**: BottomSheet 模式下，保留 Tab 切换功能。

**验收标准**:
- [x] 显示详情 / 剧集 / 评论三个 Tab
- [x] Tab 切换正常工作
- [x] 各 Tab 内容正常显示

## 非功能需求

### NFR-1: 性能

- BottomSheet 弹出动画流畅
- 复用现有 VM，不重复加载数据

### NFR-2: 兼容性

- 与现有详情页组件兼容
- 不影响现有功能

## 约束

- 复用现有的 BangumiDetailVM
- 复用现有的详情页组件（BangumiDetailHeader, BangumiContent）
- 保持与现有 UI 风格一致
