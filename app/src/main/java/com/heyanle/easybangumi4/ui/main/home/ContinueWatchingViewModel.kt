package com.heyanle.easybangumi4.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal data class ContinueWatchingKey(val source: String, val id: String, val watchedAt: Long)
internal fun CartoonInfo.continueWatchingKey() = ContinueWatchingKey(source, id, lastHistoryTime)

internal fun selectContinueWatching(
    history: List<CartoonInfo>,
    privateMode: Boolean,
    dismissed: ContinueWatchingKey?,
): CartoonInfo? {
    if (privateMode) return null
    val latest = history.asSequence()
        .filter { it.lastHistoryTime > 0L && it.lastProcessTime > 0L }
        .filter { it.lastTotalTile <= 0L || it.lastProcessTime < it.lastTotalTile }
        .maxByOrNull { it.lastHistoryTime }
    // Closing the bar hides this suggestion, rather than immediately exposing an older one.
    return latest?.takeUnless { it.continueWatchingKey() == dismissed }
}

class ContinueWatchingViewModel : ViewModel() {
    private val repository: CartoonRepository by Inject.injectLazy()
    private val preferences: SettingPreferences by Inject.injectLazy()
    private val dismissed = MutableStateFlow<ContinueWatchingKey?>(null)

    val item = combine(repository.flowAllHistory(), preferences.isInPrivate.flow(), dismissed) { history, privateMode, hidden ->
        selectContinueWatching(history, privateMode, hidden)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000, replayExpirationMillis = 0), null)

    fun dismiss() {
        dismissed.value = item.value?.continueWatchingKey()
    }
}
