package com.heyanle.easy_bangumi_cm.model.meida.local.entitie

/// 解析规则类型
enum class ParseRuleType {
    Suffix, // 前后缀匹配
    Filename, // 文件名匹配
    Regex, // 正则表达式
    DirectoryName, // 目录名
}