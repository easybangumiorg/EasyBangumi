package com.heyanle.easybangumi4.cloudsync.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cloudsync.CloudSyncManager
import com.heyanle.easybangumi4.cloudsync.CloudSyncPreferences
import com.heyanle.easybangumi4.cloudsync.CloudSyncSecretStore
import com.heyanle.easybangumi4.cloudsync.CloudSyncState
import com.heyanle.easybangumi4.cloudsync.OneDriveAccountState
import com.heyanle.easybangumi4.cloudsync.OneDriveAuthController
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncConflictStrategy
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncProvider
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CloudSyncSettingState(
    val provider: CloudSyncProvider,
    val conflictStrategy: CloudSyncConflictStrategy,
    val webDavUrl: String,
    val webDavUsername: String,
    val hasWebDavPassword: Boolean,
    val oneDrive: OneDriveAccountState,
    val operation: CloudSyncState,
    val pendingChanges: Int,
    val lastSuccessTime: Long,
)

class CloudSyncSettingViewModel : ViewModel() {
    private val preferences: CloudSyncPreferences by Inject.injectLazy()
    private val secretStore: CloudSyncSecretStore by Inject.injectLazy()
    private val manager: CloudSyncManager by Inject.injectLazy()
    private val oneDriveAuth: OneDriveAuthController by Inject.injectLazy()
    private val hasWebDavPassword = MutableStateFlow(secretStore.webDavPassword().isNotEmpty())

    val state = combine(
        preferences.provider.flow(),
        preferences.conflictStrategy.flow(),
        preferences.webDavUrl.flow(),
        preferences.webDavUsername.flow(),
        oneDriveAuth.state,
        manager.state,
        manager.pendingChangeCount,
        preferences.lastSuccessTime.flow(),
        hasWebDavPassword,
    ) { values ->
        CloudSyncSettingState(
            provider = values[0] as CloudSyncProvider,
            conflictStrategy = values[1] as CloudSyncConflictStrategy,
            webDavUrl = values[2] as String,
            webDavUsername = values[3] as String,
            hasWebDavPassword = values[8] as Boolean,
            oneDrive = values[4] as OneDriveAccountState,
            operation = values[5] as CloudSyncState,
            pendingChanges = values[6] as Int,
            lastSuccessTime = values[7] as Long,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CloudSyncSettingState(
            provider = preferences.provider.get(),
            conflictStrategy = preferences.conflictStrategy.get(),
            webDavUrl = preferences.webDavUrl.get(),
            webDavUsername = preferences.webDavUsername.get(),
            hasWebDavPassword = secretStore.webDavPassword().isNotEmpty(),
            oneDrive = oneDriveAuth.state.value,
            operation = manager.state.value,
            pendingChanges = manager.pendingChangeCount.value,
            lastSuccessTime = preferences.lastSuccessTime.get(),
        ),
    )

    fun selectProvider(provider: CloudSyncProvider) {
        preferences.provider.set(provider)
        manager.resetStatus()
    }

    fun setConflictStrategy(strategy: CloudSyncConflictStrategy) {
        preferences.conflictStrategy.set(strategy)
    }

    fun saveWebDav(url: String, username: String, password: String) {
        preferences.webDavUrl.set(url.trim())
        preferences.webDavUsername.set(username.trim())
        if (password.isNotEmpty()) {
            secretStore.setWebDavPassword(password)
            hasWebDavPassword.value = true
        }
        preferences.provider.set(CloudSyncProvider.WEBDAV)
        manager.resetStatus()
    }

    fun clearWebDavPassword() {
        secretStore.clearWebDavPassword()
        hasWebDavPassword.value = false
        manager.resetStatus()
    }

    fun signInOneDrive(activity: Activity) {
        preferences.provider.set(CloudSyncProvider.ONEDRIVE)
        oneDriveAuth.signIn(activity)
    }

    fun signOutOneDrive() {
        oneDriveAuth.signOut()
        manager.resetStatus()
    }

    fun testConnection() {
        viewModelScope.launch { manager.testConnection() }
    }

    fun upload() {
        viewModelScope.launch { manager.upload() }
    }

    fun download() {
        viewModelScope.launch { manager.download() }
    }
}
