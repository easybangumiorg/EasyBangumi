package com.heyanle.easybangumi4.ui.source_manage.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryInfo
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryPreferences
import com.heyanle.easybangumi4.plugin.source.repository.activeRepositories
import com.heyanle.easybangumi4.plugin.source.repository.isRepositoryUrlValid
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RepositoryManageViewModel : ViewModel() {

    private val repositoryPreferences: RepositoryPreferences by Inject.injectLazy()

    data class State(
        val repositories: List<RepositoryInfo> = emptyList(),
        val addError: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repositoryPreferences.repositories.requestFlow.collect { repos ->
                _state.update { it.copy(repositories = activeRepositories(repos)) }
            }
        }
    }

    fun addRepository(input: String): Boolean {
        val url = input.trim()
        if (!isRepositoryUrlValid(url)) {
            _state.update { it.copy(addError = "请输入有效的 HTTP 或 HTTPS 仓库地址") }
            return false
        }
        _state.update { it.copy(addError = null) }
        val saved = repositoryPreferences.repositories.getOrNull()
        if (saved == null) {
            _state.update { it.copy(addError = "仓库列表尚未读取完成，请稍后重试") }
            return false
        }
        val repos = saved.toMutableList()
        if (repos.any { it.url.trim() == url }) return true
        repos.add(RepositoryInfo(url = url, addedAt = System.currentTimeMillis()))
        repositoryPreferences.repositories.set(repos)
        return true
    }

    fun removeRepository(repo: RepositoryInfo) {
        val repos = repositoryPreferences.repositories.getOrNull()?.toMutableList() ?: return
        repos.removeAll { it.url.trim() == repo.url }
        repositoryPreferences.repositories.set(repos)
    }
}
