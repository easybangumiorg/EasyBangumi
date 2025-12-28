package org.easybangumi.next.shared.source.quick.utils

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.JsFunction
import com.dokar.quickjs.binding.define
import com.dokar.quickjs.binding.toJsObject
import org.easybangumi.next.lib.logger.Logger
import org.easybangumi.next.shared.source.api.utils.EventBusHelper
import org.easybangumi.next.shared.source.api.utils.HttpHelper
import org.easybangumi.next.shared.source.api.utils.NetworkHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.StringHelper
import org.easybangumi.next.shared.source.api.utils.WebViewHelper

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
 */
suspend fun QuickJs.register(preferenceHelper: PreferenceHelper) {
    define("PreferenceHelper") {
        asyncFunction("map") {
            preferenceHelper.map().toJsObject()
        }
        asyncFunction("get") {
            val key: String = it[0].toString()
            val defaultValue: String = it.getOrNull(1) as? String ?: ""
            preferenceHelper.get(key, defaultValue)
        }

        asyncFunction("put") {
            val key: String = it[0].toString()
            val value: String = it[1].toString()
            preferenceHelper.put(key, value)
        }
    }
}

suspend fun QuickJs.register(stringHelper: StringHelper) {
    define("StringHelper") {
        function("toast") {
            val message: String = it[0].toString()
            stringHelper.toast(message)
        }

        function("moeSnackBar") {
            val message: String = it[0].toString()
            stringHelper.moeSnackBar(message)
        }

        function("moeDialog") {
            val message: String = it[0].toString()
            stringHelper.moeDialog(message)
        }
    }
}

suspend fun QuickJs.register(networkHelper: NetworkHelper) {
    define("NetworkHelper") {
        function("getDefaultMacosUA") {
            networkHelper.defaultMacosUA
        }
        function("getDefaultWindowsUA") {
            networkHelper.defaultWindowsUA
        }
        function("getDefaultLinuxUA") {
            networkHelper.defaultLinuxUA
        }
        function("getDefaultAndroidUA") {
            networkHelper.defaultAndroidUA
        }
        function("getRandomUA") {
            networkHelper.randomUA
        }
    }
}

suspend fun QuickJs.register(httpHelper: HttpHelper) {

}

suspend fun QuickJs.register(logger: Logger) {

    define("Log") {
        function("d") {
            val message: String = it[0].toString()
            logger.debug(message)
        }
        function("i") {
            val message: String = it[0].toString()
            logger.info(message)
        }
        function("w") {
            val message: String = it[0].toString()
            logger.warn(message)
        }
        function("e") {
            val message: String = it[0].toString()
            logger.error(message)
        }
    }

}
