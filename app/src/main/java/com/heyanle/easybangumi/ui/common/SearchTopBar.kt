package com.heyanle.easybangumi.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier,
    placeholder: @Composable ()->Unit,
    text: MutableState<String>,
    onBack: ()->Unit,
    onSearch: (String)->Unit,
){

    val themeState by remember {
        EasyThemeController.easyThemeState
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.primary)
        .statusBarsPadding()
        .padding(8.dp, 8.dp)
        .fillMaxWidth().then(modifier)
    ){
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            leadingIcon = {
                IconButton(onClick = {
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        stringResource(id = R.string.back)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(text.value)
                }
            ),
            value = text.value,
            onValueChange = {
                text.value = it
            },
            colors =  TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                cursorColor = MaterialTheme.colorScheme.secondary,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedIndicatorColor = Color.Transparent,
                placeholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                focusedIndicatorColor = Color.Transparent,
            ),
            placeholder = placeholder,
            trailingIcon = {
                TextButton(
                    onClick = {
                        onSearch(text.value)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.secondary,
                    )
                ) {
                    Text(text = stringResource(id = R.string.search))
                }
            },
        )

    }



}