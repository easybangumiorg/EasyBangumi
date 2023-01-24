package com.heyanle.easybangumi.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.theme.EasyThemeController
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/10 16:57.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    containerColor: Color,
    placeholder: @Composable ()->Unit,
    text: MutableState<String>,
    onValueChange: (String)->Unit = {text.value = it},
    onBack: ()->Unit,
    onSearch: (String)->Unit,
    focusRequester: FocusRequester,
){


    TopAppBar(
        modifier = Modifier.then(modifier),
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = containerColor,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = {
                onBack()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    stringResource(id = R.string.back)
                )
            }
        },
        actions = {
            Crossfade(
                text.value.isNotEmpty()
            ){
                if(it){
                    Row() {
                        IconButton(onClick = {
                            text.value = ""
                            onValueChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                stringResource(id = R.string.clear)
                            )
                        }
                        IconButton(onClick = {
                            onSearch(text.value)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                stringResource(id = R.string.search)
                            )
                        }
                    }


                }
            }
        },
        title = {
            val textStyle = MaterialTheme.typography.titleLarge
            TextField(
                singleLine = true,
                textStyle = textStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch(text.value)
                    }
                ),
                value = text.value,
                onValueChange = onValueChange,
                colors =  TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedIndicatorColor = Color.Transparent,
                    placeholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    focusedIndicatorColor = Color.Transparent,
                    selectionColors = TextSelectionColors(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary)
                ),
                placeholder = {
                    CompositionLocalProvider(
                        LocalTextStyle provides textStyle
                    ) {
                        placeholder()
                    }
                },
            )

        }
    )
}