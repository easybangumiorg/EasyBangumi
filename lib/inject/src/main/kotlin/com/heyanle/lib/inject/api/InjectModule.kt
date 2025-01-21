package com.heyanle.lib.inject.api

/**
 * Created by HeYanLe on 2023/7/29 20:11.
 * https://github.com/heyanLE
 */
abstract class InjectScopedMain(val scope: InjectScope) : InjectModule {
    init {
        scope.registerInjectables()
    }
}

interface InjectModule {
    fun registerWith(intoScope: InjectScope) {
        intoScope.registerInjectables()
    }

    fun InjectScope.registerInjectables()

}
