package com.heyanle.easy_bangumi_cm.model.meida.local

import java.nio.file.Path

/// 表示单个媒体信息
class VideoFileInfo(
    name: String,
    path: Path,
    container: String?, // 容器类型
    parseRuleType: ParseRuleType, // 解析规则类型
) {
}