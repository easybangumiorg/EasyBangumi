package org.easybangumi.next.shared.plugin.component.meta

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 */

/**
 * 页面组件，代表该源支持哪些页面
 * 如果全支持，则首页会有三个 tab 代表三个页面
 * 首页 时间线 筛选
 */
enum class Page {
    HOME,           // 首页，首页页面可通过 HomeAction 定制
    TIMELINE,       // 时间线
    FILTER,         // 筛选
}

/**
 * 首页页面的元素，从上到下排列
 * 例如 Bangumi 源的首页为
 * Banner 推荐番剧轮播
 * 最近观看，点击更多跳转到历史记录模块
 * 栏目1 热度推荐，点击更多跳转筛选
 * 栏目2 今日更新，点击更多跳转时间线
 */

enum class HomeAction {
    HISTORY,        // 最近观看，数据为程序内置，源无法修改
    BANNER,         // banner 组件
    SECTION,        // 栏目 组件
}