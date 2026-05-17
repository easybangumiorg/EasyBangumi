# Tasks: add-bangumi-detail-bottom-sheet

## 实现任务

- [x] 1. 修改 BangumiDetailVM.kt，确保 panelMode 参数被正确传递
- [x] 2. 修改 BangumiDetailHeader.kt，添加 showPlayBtn 参数
- [x] 3. 修改 BangumiDetailHeader.kt，当 showPlayBtn=false 时收藏按钮移到中间位置
- [x] 4. 修改 BangumiDetailSubPage.kt，添加 interactive 参数
- [x] 5. 修改 BangumiDetailSubPage.kt，当 interactive=false 时禁用剧集交互
- [x] 6. 新建 BangumiDetailPanel.kt，创建 BottomSheet 专用详情页组件
- [x] 7. 修改 BangumiMediaPopup.kt，实现 ModalBottomSheet
- [x] 8. 修改 BangumiMediaCommonVM.kt，移除 easyTODO 调用
- [x] 9. BottomSheet 模式下顶部背景颜色与 Tab 一致（使用 surfaceContainerHigh）
- [x] 10. BottomSheet 模式下收藏按钮左对齐
- [x] 11. BottomSheet 模式下支持滚动，Header 可滚动，Tab 固定（使用 LazyColumn + stickyHeader）
- [x] 12. 测试功能完整性
