日志门面，因为日志比较重要，几乎所有模块都需要。因此是唯一允许被独立代码层级依赖的模块

jvmMain 使用 slf4j

具体到纯纯看番，desktop 使用 log4j2，  android 使用 logback，顶层模块引入