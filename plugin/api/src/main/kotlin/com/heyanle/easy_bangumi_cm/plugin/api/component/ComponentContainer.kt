package com.heyanle.easy_bangumi_cm.plugin.api.component

import kotlin.reflect.KClass


/**
 * Created by HeYanLe on 2024/12/8 22:18.
 * https://github.com/heyanLE
 */

interface ComponentContainer {

    fun <T: Component> getComponent(clazz: KClass<T>): T? = getComponent(clazz.java)

    fun <T: Component> getComponent(clazz: Class<T>): T?

}