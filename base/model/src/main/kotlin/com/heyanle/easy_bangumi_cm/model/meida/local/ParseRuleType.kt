package com.heyanle.easy_bangumi_cm.model.meida.local

/// 解析规则类型
enum class ParseRuleType {
    Suffix, // 后缀
    Filename, // 文件名
    Regex, // 正则表达式
    DirectoryName, // 目录名
}