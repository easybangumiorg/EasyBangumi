package com.heyanle.easybangumi4.ui.common

import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.heyanle.easybangumi4.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/25 19:14.
 * https://github.com/heyanLE
 */

// TODO use MD3 ModalBottomSheet instead
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MD3BottomSheet(
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    sheetContent: @Composable ColumnScope.() -> Unit,
) {


    Popup {
        ModalBottomSheetLayout(
            modifier = modifier.zIndex(1f), // always top
            sheetState = sheetState,
            sheetShape = RoundedCornerShape(16.dp, 16.dp),
            sheetElevation = 0.dp,
            sheetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            sheetContentColor = MaterialTheme.colorScheme.onSurface,
            sheetContent = {
                // DragHandle
                Box(
                    Modifier
                        .padding(vertical = 10.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .alpha(0.4f)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        .align(Alignment.CenterHorizontally)
                )
                sheetContent()
                Spacer(Modifier.navigationBarsPadding())
            },
            content = {}
        )
    }


}

// TODO 添加 header
@Composable
fun EasyBottomSheetDialog(
    onDismissRequest: ()->Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
){

    val ctx = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shape = MaterialTheme.shapes
    val dialog = remember(sheetContent) {
        BottomSheetDialog(ctx, R.style.BottomSheetDialogStyle).apply {

            val nest = NestedScrollView(ctx)
            nest.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            val composeView = ComposeView(ctx).apply {
                //setBackgroundColor(colorScheme.background.toArgb())
                setContent {
                    MaterialTheme(
                        colorScheme = colorScheme,
                        typography = typography,
                        shapes = shape
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp, 16.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Column() {
                                // DragHandle
                                Box(
                                    Modifier
                                        .padding(vertical = 10.dp)
                                        .width(32.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .alpha(0.4f)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                                        .align(Alignment.CenterHorizontally)
                                )
                                sheetContent()
                            }
                        }

                    }

                }
                val lifecycleOwner = MyLifecycleOwner()
                lifecycleOwner.performRestore(null)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                setViewTreeLifecycleOwner(lifecycleOwner)
                this.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                val viewModelStore = ViewModelStore()
                setViewTreeViewModelStoreOwner(object: ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore
                        get() = viewModelStore
                })

                val coroutineContext = AndroidUiDispatcher.CurrentThread
                val runRecomposeScope = CoroutineScope(coroutineContext)
                val reComposer = Recomposer(coroutineContext)
                this.compositionContext = reComposer
                runRecomposeScope.launch {
                    reComposer.runRecomposeAndApplyChanges()
                }
            }
            nest.addView(composeView)
            setContentView(nest)
            setOnDismissListener {
                onDismissRequest()
            }
        }
    }

    DisposableEffect(key1 = Unit){
        dialog.show()
        onDispose {
            dialog.hide()
        }
    }
}

internal class MyLifecycleOwner : SavedStateRegistryOwner {

    private var mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private var mSavedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = mSavedStateRegistryController.savedStateRegistry

    val isInitialized: Boolean
        get() = true

    override val lifecycle: Lifecycle
        get() = mLifecycleRegistry

    fun setCurrentState(state: Lifecycle.State) {
        mLifecycleRegistry.currentState = state
    }

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        mLifecycleRegistry.handleLifecycleEvent(event)
    }

    fun performRestore(savedState: Bundle?) {
        mSavedStateRegistryController.performRestore(savedState)
    }

    fun performSave(outBundle: Bundle) {
        mSavedStateRegistryController.performSave(outBundle)
    }
}