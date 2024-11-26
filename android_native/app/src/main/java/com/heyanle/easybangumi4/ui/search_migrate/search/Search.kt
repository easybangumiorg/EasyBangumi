package com.heyanle.easybangumi4.ui.search_migrate.search

import android.os.Process
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearch
import com.heyanle.easybangumi4.ui.search_migrate.search.normal.NormalSearch
import java.io.File


/**
 * Created by heyanlin on 2023/12/18.
 */
@Composable
fun Search(
    defWord: String,
    defSourceKey: String,
) {

    val nav = LocalNavController.current
    val searchVM = SearchViewModelFactory.newViewModel(defSearchKey = defWord)

    val focusRequester = remember {
        FocusRequester()
    }

    val searchEvent = searchVM.searchFlow.collectAsState()
    val searchHistory = searchVM.searchHistory.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SearchTopAppBar(
            text = searchVM.searchBarText.value,
            isGather = searchVM.isGather.value,
            focusRequester = focusRequester,
            showAction = true,
            onBack = {
                nav.popBackStack()
            },
            onSearch = {
//                val pid = Process.myPid()
////self也可以改成pid
////self也可以改成pid
//                val f1 = File("/proc/self/maps")
//                if (f1.exists() && f1.isFile) {
//                    f1.readLines().forEach {
//                        it.logi("Search")
//                    }
//                } else {
//                    //Log.d("tag_so", " cannot read so libs " + f1.exists())
//                }
               // BydsSource.generateToken().logi("Search")
                searchVM.search(it)
            },
            onTextChange = {
                searchVM.searchBarText.value = it
                if (it.isEmpty()) {
                    searchVM.search(it)
                }
            },
            onIsGatherChange = {
                searchVM.onGatherChange(it)
            }
        )

        if(searchEvent.value.isEmpty()){
            // 历史搜索页面
            SearchEmptyPage(
                historyList = searchHistory.value,
                onHistoryClick = {
                    searchVM.searchBarText.value = it
                    searchVM.search(it)
                },
                onClearHistory = {
                    searchVM.clearHistory()
                }
            )
        }else if (searchVM.isGather.value){
            // 聚合搜索
            GatherSearch(searchViewModel = searchVM)
        }else{
            // 普通搜素
            NormalSearch(defSourceKey = defSourceKey, searchViewModel = searchVM)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.SearchEmptyPage(
    historyList: List<String>,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        EmptyPage(
            modifier = Modifier
                .fillMaxSize(),
            emptyMsg = stringResource(id = R.string.please_input_keyword_to_search)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.history))
                IconButton(onClick = {
                    onClearHistory()
                }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.delete)
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                historyList.forEach {
                    Surface(
                        shadowElevation = 4.dp,
                        shape = CircleShape,
                        modifier =
                        Modifier
                            .padding(2.dp, 8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                    ) {
                        Text(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onHistoryClick(it)
                                }
                                .padding(8.dp, 4.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.W900,
                            text = it,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    text: String,
    isGather: Boolean, // 是否是聚合搜索模式
    focusRequester: FocusRequester,
    showAction: Boolean = true,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onIsGatherChange: (isGather: Boolean) -> Unit,
) {

    var showMenu by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                onBack()

            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                )
            }
        },
        title = {
            LaunchedEffect(key1 = Unit) {
                focusRequester.requestFocus()
            }
            TextField(
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch(text)
                }),
                maxLines = 1,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .horizontalScroll(
                        rememberScrollState()
                    ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                value = text,
                onValueChange = {
                    onTextChange(it)
                },
                placeholder = {
                    Text(
                        style = MaterialTheme.typography.titleLarge,
                        text = stringResource(id = R.string.please_input_keyword_to_search),
                        maxLines = 1
                    )
                })
        },
        actions = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    onTextChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        stringResource(id = R.string.clear)
                    )
                }
            }
            IconButton(onClick = {
                onSearch(text)
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    stringResource(id = R.string.search)
                )
            }



            if(showAction){
                IconButton(onClick = {
                    showMenu = !showMenu
                }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(id = R.string.more)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }) {

                    DropdownMenuItem(
                        text = {
                            Text(text = stringResource(id = R.string.gather_search))
                        },
                        onClick = {
                            onIsGatherChange(!isGather)
                        },
                        leadingIcon = {
                            if(isGather){
                                Icon(Icons.Filled.Check, contentDescription = stringResource(id = R.string.gather_search))
                            }else{
                                Box(modifier = Modifier.size(24.dp))
                            }
                        }
                    )
                }
            }


        }
    )

}