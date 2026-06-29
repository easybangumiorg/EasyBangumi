package com.heyanle.easybangumi4.ui.source_manage.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryInfo
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryPreferences
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RepositoryManageViewModel : ViewModel() {

    private val repositoryPreferences: RepositoryPreferences by Inject.injectLazy()

    data class State(
        val repositories: List<RepositoryInfo> = emptyList(),
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repositoryPreferences.repositories.requestFlow.collect { repos ->
                _state.update { it.copy(repositories = repos) }
            }
        }
    }

    fun addRepository(url: String) {
        val repos = repositoryPreferences.repositories.getOrDef().toMutableList()
        if (repos.any { it.url == url }) return
        repos.add(RepositoryInfo(url = url, addedAt = System.currentTimeMillis()))
        repositoryPreferences.repositories.set(repos)
    }

    fun removeRepository(repo: RepositoryInfo) {
        val repos = repositoryPreferences.repositories.getOrDef().toMutableList()
        repos.removeAll { it.url == repo.url }
        repositoryPreferences.repositories.set(repos)
    }
}
