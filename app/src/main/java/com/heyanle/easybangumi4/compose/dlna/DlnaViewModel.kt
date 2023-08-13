package com.heyanle.easybangumi4.compose.dlna

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.bangumi_source_api.api.component.detailed.DetailedComponent
import com.heyanle.bangumi_source_api.api.component.play.PlayComponent
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.base.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.CartoonRepository
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/11 21:15.
 * https://github.com/heyanLE
 */
class DlnaViewModel(
    private val cartoonSummary: CartoonSummary,
    private val playComponent: PlayComponent,
    private val enterData: EnterData? = null,
) : ViewModel() {

    data class EnterData(
        val lineIndex: Int = -1,
        val episode: Int = -1,
    ) {}

    sealed class DetailedState {
        object None : DetailedState()

        object Loading : DetailedState()

        class Info(
            val detail: CartoonInfo,
            val playLine: List<PlayLine>,
            val isShowPlayLine: Boolean = true,
        ) : DetailedState()

        class Error(
            val errorMsg: String,
            val throwable: Throwable?
        ) : DetailedState()
    }

    var detailedState by mutableStateOf<DetailedState>(DetailedState.None)


    sealed class PlayingState {
        object None : PlayingState()

        class Loading(
            val playLineIndex: Int,
            val playLine: PlayLine,
            val curEpisode: Int,
            val detailInfo: DetailedState.Info,
        ) : PlayingState()

        class Playing(
            val playLineIndex: Int,
            val playerInfo: PlayerInfo,
            val playLine: PlayLine,
            val curEpisode: Int,
            val detailInfo: DetailedState.Info,
        ) : PlayingState()

        class Error(
            val playLineIndex: Int,
            val errMsg: String,
            val throwable: Throwable?,
            val playLine: PlayLine,
            val curEpisode: Int,
            val detailInfo: DetailedState.Info,
        ) : PlayingState()

        fun playLine(): PlayLine? {
            return when (this) {
                None -> null
                is Loading -> playLine
                is Playing -> playLine
                is Error -> playLine
            }
        }

        fun playLineIndex(): Int? {
            return when (this) {
                None -> null
                is Loading -> playLineIndex
                is Playing -> playLineIndex
                is Error -> playLineIndex
            }
        }

        fun episode(): Int {
            return when (this) {
                None -> -1
                is Loading -> curEpisode
                is Playing -> curEpisode
                is Error -> curEpisode
            }
        }
    }

    private val cartoonRepository: CartoonRepository by Injekt.injectLazy()

    var playingState by mutableStateOf<PlayingState>(PlayingState.None)

    var selectedLineIndex by mutableIntStateOf(0)

    private var lastJob: Job? = null

    fun loadDetailed() {
        lastJob?.cancel()
        lastJob = viewModelScope.launch {
            detailedState = DetailedState.Loading
            cartoonRepository.getCartoonInfoWithPlayLines(cartoonSummary.id, cartoonSummary.source, cartoonSummary.url)
                .onOK {
                    if (!isActive) {
                        return@onOK
                    }
//                    it.data.second.loge("DetailedViewModel")
//                    it.data.second.size.loge("DetailedViewModel")
//                    it.data.second.first().episode.size.loge("DetailedViewModel")
                    val info = DetailedState.Info(
                        it.first,
                        it.second,
                        it.second !is DetailedComponent.NonPlayLine
                    )
                    detailedState = info

                    if (enterData != null) {
                        if (enterData.lineIndex in info.playLine.indices) {
                            selectedLineIndex = enterData.lineIndex
                        }

                        loadPlay(info, enterData.lineIndex, enterData.episode)
                    }
                }.onError {
                    if (!isActive) {
                        return@onError
                    }
                    detailedState = DetailedState.Error(
                        it.errorMsg,
                        it.throwable
                    )
                }

        }
    }

    fun loadPlay(
        detailInfo: DetailedState.Info,
        lineIndex: Int = -1,
        episode: Int = -1,
    ) {
        if (lineIndex !in detailInfo.playLine.indices || episode !in detailInfo.playLine[lineIndex].episode.indices) {
            return
        }
        lastJob?.cancel()
        lastJob = viewModelScope.launch {
            playingState =
                PlayingState.Loading(lineIndex, detailInfo.playLine[lineIndex], episode, detailInfo)
            playComponent.getPlayInfo(cartoonSummary, detailInfo.playLine[lineIndex], episode)
                .complete {
                    if (!isActive) {
                        return@complete
                    }
                    playingState = PlayingState.Playing(
                        lineIndex, it.data, detailInfo.playLine[lineIndex], episode, detailInfo
                    )
                    DlnaManager.playNew(it.data.uri)
                }.error {
                    if (!isActive) {
                        return@error
                    }
                    it.throwable.printStackTrace()
                    playingState = PlayingState.Error(
                        lineIndex,
                        if (it.isParserError) stringRes(
                            R.string.source_error
                        ) else stringRes(R.string.loading_error),
                        it.throwable,
                        detailInfo.playLine[lineIndex],
                        episode, detailInfo
                    )
                }
        }
    }


}

class DlnaViewModelFactory(
    private val cartoonSummary: CartoonSummary,
    private val playComponent: PlayComponent,
    private val enterData: DlnaViewModel.EnterData? = null,
) : ViewModelProvider.Factory {

    companion object {

        @Composable
        fun new(
            cartoonSummary: CartoonSummary,
            playComponent: PlayComponent,
            enterData: DlnaViewModel.EnterData? = null,
        ): DlnaViewModel {
            return viewModel<DlnaViewModel>(
                factory = DlnaViewModelFactory(
                    cartoonSummary,
                    playComponent,
                    enterData
                )
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DlnaViewModel::class.java))
            return DlnaViewModel(cartoonSummary, playComponent, enterData) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}