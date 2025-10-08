package com.heyanle.easybangumi4.ui.extension_push

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.plugin.extension.remote.ExtensionRemoteController
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * Created by heyanle on 2025/8/16
 * https://github.com/heyanLE
 */
class ExtensionRepositoryViewModel : ViewModel() {

    var dialog by mutableStateOf<Dialog?>(null)
        private set
    var repository by mutableStateOf<List<ExtensionRemoteController.Repository>>(emptyList())
        private set

    private val extensionRemoteController: ExtensionRemoteController by Inject.injectLazy()

    sealed class Dialog {

        class Delete(
            val repository: ExtensionRemoteController.Repository
        ) : Dialog()


        data object Create : Dialog()
    }

    init {
        viewModelScope.launch {
            extensionRemoteController.repositoryState.collectLatest {
                this@ExtensionRepositoryViewModel.repository = (it.okOrNull() ?: emptyList()) + ExtensionRemoteController.officeReposotory
            }
        }
    }

    fun move(from: Int, to: Int) {
        this@ExtensionRepositoryViewModel.repository = this@ExtensionRepositoryViewModel.repository.toMutableList().apply {
            add(to, removeAt(from))
        }
    }

    fun onDragEnd() {
        viewModelScope.launch {
            val ts = this@ExtensionRepositoryViewModel.repository.mapIndexed { index, cartoonTag ->
                cartoonTag.copy(order = index)
            }
            extensionRemoteController.updateRepository(ts)
        }
    }

    fun dialogDelete(cartoonTag: ExtensionRemoteController.Repository) {
        dialog = Dialog.Delete(cartoonTag)
    }



    fun dialogCreate() {
        dialog = Dialog.Create
    }


    fun dialogDismiss() {
        dialog = null
    }

    fun onDelete(repository: ExtensionRemoteController.Repository) {

        viewModelScope.launch {
            extensionRemoteController.deleteRepository(repository)

        }
    }




    fun onCreate(url: String) {
        viewModelScope.launch {
            extensionRemoteController.addRepository(url)
        }
    }
}