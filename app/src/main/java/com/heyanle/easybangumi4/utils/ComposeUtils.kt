package com.heyanle.easybangumi4.utils

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Parcel
import android.os.Parcelable
import android.view.OrientationEventListener
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.paging.compose.LazyPagingItems
import coil.Coil
import coil.request.ImageRequest
import com.heyanle.easybangumi4.APP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Created by LoliBall on 2023/3/1 17:59.
 * https://github.com/WhichWho
 */


fun Modifier.navigationBarsPaddingOnLandscape() = composed {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> this.navigationBarsPadding()
        else -> this
    }
}

@Composable
fun screenSize() = LocalDensity.current.run {
    Size(
        LocalConfiguration.current.screenWidthDp.dp.toPx(),
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    )
}

@Composable
fun screenSizeDp() = LocalDensity.current.run {
    DpSize(
        LocalConfiguration.current.screenWidthDp.dp,
        LocalConfiguration.current.screenHeightDp.dp
    )
}

@Composable
fun scaleHelper(
    scale1: MutableState<Float> = mutableStateOf(1f),
    offset1: MutableState<Offset> = mutableStateOf(Offset.Zero),
    size1: MutableState<Offset> = mutableStateOf(Offset.Zero),
    imageSize1: MutableState<Offset> = mutableStateOf(Offset.Zero),
    onLongPress: (Offset) -> Unit = {},
    onClick: (Offset) -> Unit = {}
): ScaleModifiers {
    var scale by remember { scale1 }
    var offset by remember { offset1 }
    var size by remember { size1 }
    var image by remember { imageSize1 }

    return ScaleModifiers(
        onSizeChange = Modifier.onSizeChanged {
            size = Offset(it.width.toFloat(), it.height.toFloat())
        },
        onGetImageSize = {
            image = it
        },
        onTap = Modifier.pointerInput("tap") {
            detectTapGestures(
                onTap = onClick,
                onLongPress = onLongPress,
                onDoubleTap = {
                    if (scale == 1f) {
                        scale = 2f
                        offset = size / 2f - it
                    } else {
                        scale = 1f
                        offset = Offset.Zero
                    }
                })
        },
        onTransform = Modifier
            .pointerInput("transform") {
                detectTransformGestures(
                    panZoomLock = true,
                    onGesture = { centroid, pan, zoom, _ ->
                        scale = (scale * zoom).coerceAtLeast(1f)
                        offset = offset * zoom + pan + (size / 2f - centroid) * (zoom - 1)
                        //防止越界
                        val range = size / 2f * (scale - 1)
                        offset = Offset(
                            offset.x.coerceIn(-range.x, range.x),
                            offset.y.coerceIn(-range.y, range.y)
                        )
                    }
                )
            },
        graphicsLayer = Modifier.graphicsLayer {
            translationX = offset.x
            translationY = offset.y
            scaleX = scale
            scaleY = scale
        }
    )
}

data class ScaleModifiers(
    val onSizeChange: Modifier,
    val onGetImageSize: (Offset) -> Unit,
    val onTap: Modifier,
    val onTransform: Modifier,
    val graphicsLayer: Modifier
)

@Composable
fun nestedScrollHelper(
    initToolbarHeight: Dp = 0.dp
): NestedScrollModifiers {
    val density = LocalDensity.current
    var toolbarHeight by remember { mutableStateOf(initToolbarHeight) }
    val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y // y方向的偏移量
                val newOffset = toolbarOffsetHeightPx.value + delta
                val toolbarHeightPx = with(density) { toolbarHeight.roundToPx().toFloat() }
                toolbarOffsetHeightPx.value = newOffset.coerceIn(-toolbarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }
    return NestedScrollModifiers(
        toolbarHeight = toolbarHeight,
        connection = Modifier.nestedScroll(nestedScrollConnection),
        onSizeChange = Modifier.onSizeChanged {
            toolbarHeight = with(density) { it.height.toDp() }
        },
        offset = Modifier.offset {
            IntOffset(
                x = 0,
                y = toolbarOffsetHeightPx.value.roundToInt()
            )
        }
    )
}

data class NestedScrollModifiers(
    var toolbarHeight: Dp,
    val connection: Modifier,
    val onSizeChange: Modifier,
    val offset: Modifier
)

@OptIn(ExperimentalFoundationApi::class)
public fun <T : Any> LazyStaggeredGridScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyStaggeredGridScope.(value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(item)
            }
        }
    ) { index ->
        itemContent(items[index])
    }
}

@SuppressLint("BanParcelableUsage")
private data class PagingPlaceholderKey(private val index: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
            object : Parcelable.Creator<PagingPlaceholderKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingPlaceholderKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
            }
    }
}

//@Composable
//fun coilGifImageLoader() = ImageLoader.Builder(LocalContext.current)
//    .components {
//        if (Build.VERSION.SDK_INT >= 28) {
//            add(ImageDecoderDecoder.Factory())
//        } else {
//            add(GifDecoder.Factory())
//        }
//    }
//    .build()



@Composable
fun OnOrientationEvent(
    rate: Int = SensorManager.SENSOR_DELAY_NORMAL,
    changeCD: Int = 300,
    onEvent: (listener: OrientationEventListener, orientation: Int) -> Unit
) {
    val ctx = LocalContext.current
    val cdAction = remember (changeCD) {
        CDAction(changeCD)
    }
    val listener = remember(ctx) {
        object : OrientationEventListener(ctx, rate) {
            override fun onOrientationChanged(orientation: Int) {
                cdAction.action {
                    onEvent(this, orientation)
                }
            }
        }
    }
    DisposableEffect(key1 = Unit) {
        listener.enable()
        onDispose {
            listener.disable()
        }
    }

}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues =
    PaddingValues(
        top = calculateTopPadding() + other.calculateTopPadding(),
        bottom = calculateBottomPadding() + other.calculateBottomPadding(),
        start = calculateStartPadding(LocalLayoutDirection.current) + other.calculateStartPadding(
            LocalLayoutDirection.current
        ),
        end = calculateEndPadding(LocalLayoutDirection.current) + other.calculateEndPadding(
            LocalLayoutDirection.current
        )
    )

suspend fun preloadImage(data: List<Any>, delayTime: Long = 300) = withContext(Dispatchers.IO) {
    launch {
        delay(delayTime)
        data.forEach {
            Coil.imageLoader(APP).enqueue(
                ImageRequest.Builder(APP)
                    .data(it)
                    .build()
            )
        }
    }
}

@Composable
@Suppress("NOTHING_TO_INLINE")
inline fun <T> rememberStateOf(value: T): State<T> {
    return remember { stateOf(value) }
}

// mutableStateOf()
class stateOf<T>(override val value: T) : State<T>