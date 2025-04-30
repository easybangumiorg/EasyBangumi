package org.easybangumi.next

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory


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
 * 比 shared 中的 Scheduler 执行早，用以初始化平台特化内容
 */
object Desktop {

    fun onInit() {
        initLog4j()
    }

    private fun initLog4j() {

        // 1. 构建配置
        val builder = ConfigurationBuilderFactory.newConfigurationBuilder()
        builder.setStatusLevel(Level.INFO) // 设置内部日志级别
        builder.setConfigurationName("CodeConfig")


        // 2. 定义 Appender（输出到控制台）
        val consoleAppender = builder.newAppender("Console", "CONSOLE")
            .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
            .add(
                builder.newLayout("PatternLayout")
                    .addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n")
            )


        // 3. 添加过滤器（可选：过滤特定日志）
        val filter = builder.newFilter("RegexFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
            .addAttribute("regex", ".*IMKInputSession_Legacy.*")
        consoleAppender.add(filter)


        // 4. 将 Appender 添加到配置
        builder.add(consoleAppender)


        // 5. 配置 Root Logger
        builder.add(
            builder.newRootLogger(Level.INFO)
                .add(builder.newAppenderRef("Console"))
        )


        // 6. 初始化 Log4j2 配置
        val config = builder.build()
        Configurator.setRootLevel(Level.INFO)
        Configurator.initialize(config)
        config.start()

    }

}