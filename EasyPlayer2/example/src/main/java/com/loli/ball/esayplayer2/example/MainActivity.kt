package com.loli.ball.esayplayer2.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.*
import loli.ball.easyplayer2.utils.loge

/**
 * Created by LoliBall on 2023/3/25 19:13.
 * https://github.com/WhichWho
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = "https://media-oss.plasticmemory.net/m3u8/1693282570265.m3u8"

        val exo = ExoPlayer.Builder(this).build()
        exo.setMediaItem(MediaItem.fromUri(url))
        exo.prepare()
        exo.play()

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val theme = if (darkTheme) DarkColorScheme else LightColorScheme
            val controller = rememberSystemUiController()
            LaunchedEffect(Unit) {
                controller.setStatusBarColor(Color.Transparent, !darkTheme)
                controller.setNavigationBarColor(Color.Transparent)
            }
            MaterialTheme(theme) {
                Surface(Modifier.fillMaxSize()) {
                    Box {
                        Content(exo)
                    }
                }
            }
        }
    }

}

@Composable
private fun Content(exo: ExoPlayer) {

    val controlVM = ControlViewModelFactory.viewModel(exo)

    EasyPlayerScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red),
        vm = controlVM,
        videoFloat = {
//            LaunchedEffect(key1 = CartoonPlayingManager.state) {
//                when (CartoonPlayingManager.state) {
//                    is CartoonPlayingManager.PlayingState.Playing -> {
//                        it.onPrepare()
//                        // CartoonPlayingManager.trySaveHistory()
//                    }
//
//                    is CartoonPlayingManager.PlayingState.Loading -> {}
//                    is CartoonPlayingManager.PlayingState.Error -> {}
//                    else -> {}
//                }
//            }
//            when (val state = CartoonPlayingManager.state) {
//                is CartoonPlayingManager.PlayingState.Playing -> {}
//                is CartoonPlayingManager.PlayingState.Loading -> {
//                    LoadingPage(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clickable { }
//                    )
//                }
//
//                is CartoonPlayingManager.PlayingState.Error -> {
//                    ErrorPage(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        errorMsg = state.errMsg,
//                        clickEnable = true,
//                        onClick = {
//                            CartoonPlayingManager.defaultScope.launch {
//                                CartoonPlayingManager.refresh()
//                            }
//                        }
//                    )
//                }
//
//                else -> {}
//            }
//            if (!it.isFullScreen) {
//                FilledIconButton(
//                    modifier = Modifier.padding(8.dp),
//                    colors = IconButtonDefaults.iconButtonColors(
//                        containerColor = Color.Black.copy(0.6f),
//                        contentColor = Color.White
//                    ),
//                    onClick = {
//                        nav.popBackStack()
//                    }) {
//                    Icon(
//                        imageVector = Icons.Filled.KeyboardArrowLeft,
//                        stringResource(id = com.heyanle.easy_i18n.R.string.back)
//                    )
//                }
//            }


        },
        control = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                // 手势
                GestureControllerWithFast(
                    vm = it,
                    modifier = Modifier.fillMaxSize(),
                    longTouchText = "X2",
                    fastWinDelay = 500
                )

                // 顶部工具栏
                ElectricityTopBar(
                    vm = it,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                )

                // 底部工具栏
                SimpleBottomBarWithSeekBar(
                    vm = it,
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    paddingValues = PaddingValues(4.dp, 8.dp)
                ) {

                }

                // 锁定按钮
                LockBtn(vm = it)

                // 加载按钮
                ProgressBox(vm = it)
            }
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {

            Button(onClick = { controlVM.setSpeed(1.0f) }) {
                Text(text = "1.0")
            }
            Button(onClick = { controlVM.setSpeed(2.0f) }) {
                Text(text = "2.0")
            }
            val position = controlVM.horizontalScrollPosition

            Row {
                TimeSlider(
                    during = controlVM.during,
                    position = position,
                    onValueChange = {
                        // "onValueChange".loge("EasyPlayerExtends")
                        controlVM.onPositionChange(it)
                    },
                    onValueChangeFinish = {
                        //"onValueChangeFinish".loge("EasyPlayerExtends")
                        controlVM.onActionUP()
                    }
                )
            }


            val scope = rememberCoroutineScope()
            val source = remember { MutableInteractionSource() }

            val isDragged by source.collectIsDraggedAsState()
            val isPress by source.collectIsPressedAsState()
            var isChange by remember {
                mutableStateOf(false)
            }


            LaunchedEffect(key1 = Unit) {
                launch {
                    snapshotFlow {
                        (!isDragged && !isPress) to isChange
                    }.collect {
                        if (it.first) {
                            controlVM.onActionUPScope()
                            isChange = false
                        }
                    }
                }
                launch {
                    snapshotFlow {
                        isPress to isChange
                    }.collectLatest {
                        if(!isPress){
                            isChange = false
                            controlVM.onActionUPScope()
                        }
                    }

                }
            }

            Slider(
                modifier = Modifier
                    .background(Color.Red),
                value = controlVM.horizontalScrollPosition,
                onValueChange = {
                    isChange = true
                    controlVM.onPositionChange(it)
                    //controlVM.horizontalScrollPosition = it
                },
                onValueChangeFinished = {
                    //controlVM//.onActionUP()
                    "onValueChangeFinish".loge("EasyPlayerExtends")
                },
                valueRange = 0F..controlVM.during.toFloat().coerceAtLeast(0F),
                interactionSource = source
            )


        }

    }
}


val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Green500 = Color(0xFF4CAF50)

val Red500 = Color(0xFFF44336)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
