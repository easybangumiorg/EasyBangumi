package org.easybangumi.next.shared.foundation.view_model

import androidx.compose.runtime.Composable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

/**
 * Created by heyanlin on 2025/3/10.
 */
@Composable
public inline fun <reified VM : ViewModel> easyVM(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    factory: ViewModelProvider.Factory? = null,
    extras: CreationExtras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
        viewModelStoreOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }
): VM = viewModel(
    viewModelStoreOwner,
    key,
    factory ?: ViewModelFactory.NonArgViewModelFactory(),
    extras
)


@Composable
public inline fun <reified VM : ViewModel> easyVM(
    vararg arg: Any
): VM {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    return viewModel(
        viewModelStoreOwner,
        null,
        ViewModelFactory.ArgViewModelFactory(arg),
        if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
            viewModelStoreOwner.defaultViewModelCreationExtras
        } else {
            CreationExtras.Empty
        }
    )
}

object ViewModelFactory {

    class ViewModelFactoryException(
        clazz: KClass<out ViewModel>,
        exception: Exception,
    ) : Exception("ViewModelFactoryException ${clazz.simpleName} ${exception.message}")

    class NonArgViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return modelClass.createInstance()
        }
    }

    class ArgViewModelFactory(private val arg1: Array<out Any>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            try {
                return modelClass.primaryConstructor!!.call(*arg1)
            } catch (e: Exception) {
                e.printStackTrace()
                throw ViewModelFactoryException(modelClass, e)
            }

        }
    }


}
