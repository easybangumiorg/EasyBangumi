# Design: add-bangumi-detail-bottom-sheet

## 架构

### 组件结构

```
BangumiMediaPopup.kt
└── ModalBottomSheet
    └── BangumiDetailPanel.kt (新建)
        ├── BangumiDetailHeader (复用)
        │   └── showPlayBtn = false
        │   └── 收藏按钮移到中间位置
        ├── BangumiDetailTab (复用)
        └── BangumiContent (复用)
            └── interactive = false
            └── 剧集页不响应点击
```

### 数据流

```
BangumiMediaCommonVM
├── bangumiDetailVM (复用)
│   ├── subjectRepository → BgmSubject
│   ├── characterRepository → BgmCharacter
│   ├── personRepository → BgmPerson
│   └── bgmCollectInfoVM → 收藏状态
└── popupState → Popup.BangumiDetailPanel
```

## 接口变更

### BangumiDetailHeader

```kotlin
@Composable
fun BangumiDetailHeader(
    modifier: Modifier,
    coverUrl: String,
    contentPaddingTop: Dp,
    isHeaderPin: Boolean = false,
    subjectState: DataState<BgmSubject>,
    bgmCollectionState: DataState<BgmCollectResp> = DataState.none(),
    cartoonInfo: CartoonInfo? = null,
    onCollectClick: () -> Unit,
    onPlayClick: () -> Unit,
    showPlayBtn: Boolean = true,  // 新增参数
)
```

当 `showPlayBtn = false` 时：
- 不显示播放按钮
- 收藏按钮移到中间位置（原来播放按钮的位置）

### BangumiDetailSubEpisodePage

```kotlin
@Composable
fun BangumiDetailSubEpisodePage(
    modifier: Modifier = Modifier,
    vm: BangumiDetailVM,
    interactive: Boolean = true,  // 新增参数
)
```

当 `interactive = false` 时：
- 不显示播放图标
- 不响应点击事件

## 状态管理

### BottomSheet 状态

使用 `rememberModalBottomSheetState()` 管理 BottomSheet 的显示/隐藏状态。

### 弹窗状态

复用现有的 `BangumiMediaCommonVM.Popup.BangumiDetailPanel` 状态。

## UI/UX 设计

### BottomSheet 布局

```
┌─────────────────────────────────────────┐
│  ┌───────┐  标题                        │
│  │ 封面  │  日期 / 集数                 │
│  │       │  评分                        │
│  └───────┘  Rank                        │
│              [收藏] ← 中间位置           │
├─────────────────────────────────────────┤
│  [详情]  [剧集]  [评论]                  │
├─────────────────────────────────────────┤
│                                         │
│  内容区域                                │
│  (根据 Tab 切换)                         │
│                                         │
└─────────────────────────────────────────┘
```

### 交互行为

1. 点击 MoreHoriz 按钮 → 弹出 BottomSheet
2. 下滑或点击遮罩 → 关闭 BottomSheet
3. 剧集列表 → 只读，不可点击
4. 收藏按钮 → 正常工作
5. Tab 切换 → 正常工作
