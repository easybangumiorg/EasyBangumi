package com.heyanle.easybangumi4.ui.source_manage.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryController
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryEntry
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryPreferences
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RepositorySourceViewModel : ViewModel() {

    private val repositoryController: RepositoryController by Inject.injectLazy()
    private val repositoryPreferences: RepositoryPreferences by Inject.injectLazy()

    data class State(
        val entries: List<RepositoryEntry> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val installState: InstallState = InstallState.Idle,
    )

    sealed class InstallState {
        data object Idle : InstallState()
        data class Installing(val key: String) : InstallState()
        data class Success(val message: String) : InstallState()
        data class Error(val message: String) : InstallState()
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repositoryController.state.collectLatest { repoState ->
                when (repoState) {
                    is RepositoryController.State.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null, entries = emptyList()) }
                    }
                    is RepositoryController.State.Loaded -> {
                        _state.update { it.copy(isLoading = false, error = null, entries = repoState.entries) }
                    }
                    is RepositoryController.State.Error -> {
                        _state.update { it.copy(isLoading = false, error = repoState.message, entries = emptyList()) }
                    }
                }
            }
        }
    }

    fun refresh() {
        repositoryController.refresh()
    }

    fun installSource(entry: RepositoryEntry) {
        viewModelScope.launch {
            _state.update { it.copy(installState = InstallState.Installing(entry.key)) }
            val result = repositoryController.installSource(entry)
            result.onOK {
                _state.update { it.copy(installState = InstallState.Success("${entry.label} installed")) }
            }.onError { error ->
                _state.update { it.copy(installState = InstallState.Error(error.errorMsg)) }
            }
        }
    }

    fun clearInstallState() {
        _state.update { it.copy(installState = InstallState.Idle) }
    }

    fun getRepoCount(): Int {
        return repositoryPreferences.repositories.getOrDef().size
    }
}
