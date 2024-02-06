package com.heyanle.easybangumi4.ui.dlna

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.dlna.EasyDlna
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.cybergarage.upnp.Device

/**
 * Created by heyanlin on 2024/2/6 15:44.
 */
class DlnaPlayingViewModel : ViewModel() {


    private val _playingState = MutableStateFlow<DlnaPlayingState>(
        DlnaPlayingState()
    )
    val playingState = _playingState.asStateFlow()

    data class DlnaPlayingState(
        val isLoading: Boolean = true,
        val device: Device? = null,
        val playerInfo: PlayerInfo? = null,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val errorThrowable: Throwable? = null
    )


    private val searchJob: Job? = null
    private var lastJob: Job? = null

    private val easyDlna: EasyDlna by Injekt.injectLazy()
    private val sourceStateCase: SourceStateCase by Injekt.injectLazy()

    val showDeviceDialog = mutableStateOf(false)
    val deviceList = easyDlna.deviceList

    private val tempMap = HashMap<CartoonPlayViewModel.CartoonPlayState, PlayerInfo>()

    private var device: Device? = null
    private var playState: CartoonPlayViewModel.CartoonPlayState? = null

    init {
        viewModelScope.launch {
            _playingState.collectLatest {
                if(!it.isError && !it.isLoading && it.playerInfo != null && it.device != null){
                    easyDlna.setUrl(it.device, it.playerInfo.uri)
                    easyDlna.play(it.device)
                }
            }
        }

    }

    fun changeDevice(device: Device) {
        _playingState.update {
            it.copy(
                device = device
            )
        }
    }

    fun search() {
        viewModelScope.launch {
            easyDlna.init()
            easyDlna.search()
        }
    }

    fun tryPlay(){
        viewModelScope.launch {
            val current = _playingState.value
            current.device ?: return@launch
            easyDlna.play(current.device)
        }
    }

    fun tryPause(){
        viewModelScope.launch {
            val current = _playingState.value
            current.device ?: return@launch
            easyDlna.pause(current.device)
        }
    }

    fun tryStop(){
        viewModelScope.launch {
            val current = _playingState.value
            current.device ?: return@launch
            easyDlna.stop(current.device)
        }
    }

    fun tryRefresh(){
        viewModelScope.launch {
            val current = _playingState.value
            current.device ?: return@launch
            current.playerInfo ?: return@launch
            easyDlna.setUrl(current.device, current.playerInfo.uri)
            easyDlna.play(current.device)
        }
    }

    fun changePlay(cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState) {
        lastJob?.cancel()
        lastJob = viewModelScope.launch {
            val t = tempMap[cartoonPlayingState]
            if (t != null) {
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isError = false,
                        playerInfo = t
                    )
                }
                return@launch
            }
            val play = sourceStateCase.awaitBundle().play(cartoonPlayingState.cartoonSummary.source)
            if (play == null) {
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMsg = stringRes(R.string.source_not_found)
                    )
                }
                return@launch
            }
            play.getPlayInfo(
                cartoonPlayingState.cartoonSummary,
                cartoonPlayingState.playLine.playLine,
                cartoonPlayingState.episode
            )
                .complete { res ->
                    yield()
                    tempMap[cartoonPlayingState] = res.data
                    _playingState.update {
                        it.copy(
                            isLoading = false,
                            isError = false,
                            playerInfo = res.data
                        )
                    }
                }
                .error {
                    yield()
                    _playingState.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMsg = it.errorMsg,
                            errorThrowable = it.errorThrowable
                        )
                    }
                }
        }

    }

    fun onEnter(){
        viewModelScope.launch {
            easyDlna.init()
        }
    }

    fun onDispose(){
        easyDlna.release()
    }

    override fun onCleared() {
        easyDlna.release()
        super.onCleared()
    }
}