package com.heyanle.easybangumi4.cloudsync

import android.app.Activity
import android.app.Application
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.cloudsync.remote.CloudSyncRemoteException
import com.heyanle.easybangumi4.cloudsync.remote.OneDriveTokenProvider
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OneDriveAccountState(
    val configured: Boolean = false,
    val initializing: Boolean = false,
    val signedIn: Boolean = false,
    val accountName: String = "",
    val message: String = "",
)

/** Application-scoped MSAL single-account bridge used by the OneDrive remote store and UI. */
class OneDriveAuthController(
    private val application: Application,
    private val preferences: CloudSyncPreferences,
) : OneDriveTokenProvider {
    private val configured = BuildConfig.ONEDRIVE_CLIENT_ID.isNotBlank() &&
        BuildConfig.ONEDRIVE_REDIRECT_URI.isNotBlank()
    private val initialization = CompletableDeferred<ISingleAccountPublicClientApplication>()
    private val _state = MutableStateFlow(
        OneDriveAccountState(
            configured = configured,
            initializing = configured,
            accountName = preferences.oneDriveAccountName.get(),
            message = if (configured) "正在初始化 OneDrive" else "尚未配置 Microsoft Entra 客户端",
        )
    )
    val state = _state.asStateFlow()

    init {
        if (configured) initialize() else initialization.cancel()
    }

    fun signIn(activity: Activity) {
        if (!configured) {
            _state.value = _state.value.copy(message = "请先配置 onedrive.clientId 与 onedrive.redirectUri")
            return
        }
        val client = initialization.getCompletedOrNull()
        if (client == null) {
            _state.value = _state.value.copy(message = "OneDrive 正在初始化，请稍后重试")
            return
        }
        _state.value = _state.value.copy(message = "正在登录 OneDrive")
        val callback = object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                publishAccount(authenticationResult.account)
            }

            override fun onError(exception: MsalException) {
                publishError("OneDrive 登录失败", exception)
            }

            override fun onCancel() {
                _state.value = _state.value.copy(message = "已取消 OneDrive 登录")
            }
        }
        val parameters = SignInParameters.builder()
            .withActivity(activity)
            .withScopes(SCOPES)
            .withCallback(callback)
            .build()
        client.signIn(parameters)
    }

    fun signOut() {
        val client = initialization.getCompletedOrNull()
        if (client == null) {
            clearAccount("已退出")
            return
        }
        client.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() = clearAccount("已退出")
            override fun onError(exception: MsalException) = publishError("OneDrive 退出失败", exception)
        })
    }

    override suspend fun accessToken(): String {
        if (!configured) throw notConfiguredError()
        val client = runCatching { initialization.await() }.getOrElse {
            throw CloudSyncRemoteException(
                "OneDrive 初始化失败：${it.message.orEmpty()}",
                CloudSyncRemoteException.Kind.AUTH_REQUIRED,
                it,
            )
        }
        val account = currentAccount(client) ?: throw CloudSyncRemoteException(
            "请先登录 OneDrive",
            CloudSyncRemoteException.Kind.AUTH_REQUIRED,
        )
        return acquireTokenSilently(client, account)
    }

    private fun initialize() {
        val configFile = runCatching { createConfigurationFile() }.getOrElse {
            initialization.completeExceptionally(it)
            publishError("OneDrive 初始化失败", it)
            return
        }
        PublicClientApplication.createSingleAccountPublicClientApplication(
            application,
            configFile,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    initialization.complete(application)
                    application.getCurrentAccountAsync(
                        object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
                            override fun onAccountLoaded(activeAccount: IAccount?) {
                                if (activeAccount == null) clearAccount("尚未登录") else publishAccount(activeAccount)
                            }

                            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                                if (currentAccount == null) clearAccount("尚未登录") else publishAccount(currentAccount)
                            }

                            override fun onError(exception: MsalException) {
                                publishError("OneDrive 账号读取失败", exception)
                            }
                        },
                    )
                }

                override fun onError(exception: MsalException) {
                    initialization.completeExceptionally(exception)
                    publishError("OneDrive 初始化失败", exception)
                }
            },
        )
    }

    private suspend fun currentAccount(
        client: ISingleAccountPublicClientApplication,
    ): IAccount? = suspendCancellableCoroutine { continuation ->
        client.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                if (continuation.isActive) continuation.resume(activeAccount)
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) = Unit

            override fun onError(exception: MsalException) {
                if (continuation.isActive) continuation.resumeWithException(exception)
            }
        })
    }

    private suspend fun acquireTokenSilently(
        client: ISingleAccountPublicClientApplication,
        account: IAccount,
    ): String = suspendCancellableCoroutine { continuation ->
        val callback = object : SilentAuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    publishAccount(authenticationResult.account)
                    if (continuation.isActive) continuation.resume(authenticationResult.accessToken)
                }

                override fun onError(exception: MsalException) {
                    publishError("OneDrive 登录已失效", exception)
                    if (continuation.isActive) continuation.resumeWithException(
                        CloudSyncRemoteException(
                            "OneDrive 登录已失效，请重新登录",
                            CloudSyncRemoteException.Kind.AUTH_REQUIRED,
                            exception,
                        )
                    )
                }
            }
        val parameters = AcquireTokenSilentParameters.Builder()
            .withScopes(SCOPES)
            .forAccount(account)
            .fromAuthority(account.authority)
            .withCallback(callback)
            .build()
        client.acquireTokenSilentAsync(parameters)
    }

    private fun createConfigurationFile(): File {
        val authorities = JSONArray().put(
            JSONObject()
                .put("type", "AAD")
                .put(
                    "audience",
                    JSONObject()
                        .put("type", "AzureADandPersonalMicrosoftAccount")
                        .put("tenant_id", "common"),
                )
        )
        val config = JSONObject()
            .put("client_id", BuildConfig.ONEDRIVE_CLIENT_ID)
            .put("redirect_uri", BuildConfig.ONEDRIVE_REDIRECT_URI)
            .put("account_mode", "SINGLE")
            .put("broker_redirect_uri_registered", false)
            .put("authorities", authorities)
        return File(application.cacheDir, "onedrive_msal_config.json").apply {
            writeText(config.toString(), Charsets.UTF_8)
        }
    }

    private fun publishAccount(account: IAccount) {
        val name = account.username.orEmpty()
        preferences.oneDriveAccountName.set(name)
        _state.value = OneDriveAccountState(
            configured = true,
            signedIn = true,
            accountName = name,
            message = "已登录",
        )
    }

    private fun clearAccount(message: String) {
        preferences.oneDriveAccountName.set("")
        _state.value = OneDriveAccountState(configured = configured, message = message)
    }

    private fun publishError(prefix: String, throwable: Throwable) {
        _state.value = _state.value.copy(
            initializing = false,
            signedIn = false,
            message = "$prefix：${throwable.message.orEmpty()}",
        )
    }

    private fun notConfiguredError() = CloudSyncRemoteException(
        "OneDrive 尚未配置",
        CloudSyncRemoteException.Kind.NOT_CONFIGURED,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> CompletableDeferred<T>.getCompletedOrNull(): T? =
        if (isCompleted && !isCancelled) runCatching { getCompleted() }.getOrNull() else null

    private companion object {
        val SCOPES = listOf("Files.ReadWrite.AppFolder")
    }
}
