package com.heyanle.easybangumi.ui.player

import androidx.compose.runtime.mutableStateOf
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.home.home.AnimHomeViewModel
import com.heyanle.easybangumi.ui.home.star.AnimStarViewModel
import com.heyanle.easybangumi.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/1/11 22:02.
 * https://github.com/heyanLE
 */
class DetailController(val bangumiSummary: BangumiSummary) {

    // 番剧详情状态
    sealed class DetailStatus {
        object None : DetailStatus()
        class Loading(val event: DetailEvent) : DetailStatus()
        class Error(val event: DetailEvent, val errorMsg: String, val error: Throwable?) :
            DetailStatus()

        class Completely(val event: DetailEvent, val bangumiDetail: BangumiDetail) : DetailStatus()
    }

    // 刷新事件
    sealed class DetailEvent {
        object Init : DetailEvent()
        class LoadDetail(val bangumiSummary: BangumiSummary) : DetailEvent()
    }

    private val eventFlow = MutableStateFlow<DetailEvent>(
        DetailEvent.Init
    )

    private val _detailStatus = MutableStateFlow<DetailStatus>(DetailStatus.None)
    val detailFlow: StateFlow<DetailStatus> = _detailStatus


    val isBangumiStar = mutableStateOf(false)

    suspend fun load() {
        eventFlow.emit(DetailEvent.LoadDetail(bangumiSummary))
    }

    suspend fun init() {
        eventFlow.collectLatest { detail ->
            when (detail) {
                DetailEvent.Init -> {
                    _detailStatus.emit(DetailStatus.None)
                }

                is DetailEvent.LoadDetail -> {
                    kotlin.runCatching {
                        _detailStatus.emit(DetailStatus.Loading(detail))
                        AnimSourceFactory.requireDetail(detail.bangumiSummary.source)
                            .detail(detail.bangumiSummary)
                            .complete {
                                val isStar = withContext(Dispatchers.IO) {
                                    EasyDB.database.bangumiStarDao().getBySourceDetailUrl(
                                        it.data.source,
                                        it.data.detailUrl
                                    ) != null
                                }
                                isBangumiStar.value = isStar
                                _detailStatus.emit(DetailStatus.Completely(detail, it.data))
                            }.error {
                                it.throwable.printStackTrace()
                                _detailStatus.emit(
                                    DetailStatus.Error(
                                        detail,
                                        if (it.isParserError) stringRes(R.string.source_error) else stringRes(
                                            R.string.loading_error
                                        ),
                                        it.throwable
                                    )
                                )
                            }
                    }.onFailure {
                        it.printStackTrace()
                        _detailStatus.emit(
                            DetailStatus.Error(
                                detail,
                                stringRes(R.string.loading_error),
                                it
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun setBangumiStar(isStar: Boolean, bangumiDetail: BangumiDetail) {
        if (isStar) {
            withContext(Dispatchers.IO) {
                EasyDB.database.bangumiStar.apply {
                    val old = getBySourceDetailUrl(bangumiDetail.source, bangumiDetail.detailUrl)
                    if (old == null) {
                        insert(BangumiStar.fromBangumi(bangumiDetail))
                    } else {

                        modify(
                            old.copy(
                                name = bangumiDetail.name,
                                cover = bangumiDetail.cover,
                                source = bangumiDetail.source,
                                createTime = System.currentTimeMillis()
                            )
                        )
                    }
                }

            }
            AnimStarViewModel.refresh()
            isBangumiStar.value = true
        } else {
            withContext(Dispatchers.IO) {
                EasyDB.database.bangumiStarDao()
                    .deleteBySourceDetailUrl(bangumiDetail.source, bangumiDetail.detailUrl)
            }
            AnimStarViewModel.refresh()
            isBangumiStar.value = false
        }
    }

}