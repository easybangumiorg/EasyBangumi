package com.heyanle.injekt.api

/**
 * Created by HeYanLe on 2023/7/29 20:11.
 * https://github.com/heyanLE
 */
abstract class InjektScopedMain(val scope: InjektScope) : InjektModule {
    init {
        scope.registerInjectables()
    }
}

interface InjektModule {
    fun registerWith(intoScope: InjektScope) {
        intoScope.registerInjectables()
    }

    fun InjektScope.registerInjectables()

}
