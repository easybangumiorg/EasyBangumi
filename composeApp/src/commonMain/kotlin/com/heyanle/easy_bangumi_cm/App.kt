package com.heyanle.easy_bangumi_cm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.heyanle.easy_bangumi_cm.media.entity.TestInfo
import com.heyanle.easy_bangumi_cm.media.repository.dao.MediaInfoDao
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import easybangumi.composeapp.generated.resources.Res
import easybangumi.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
@Preview
fun App() {

    val dao by remember { koin.inject<MediaInfoDao>() }
    val testList = dao.flowTest().collectAsState(emptyList())
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                scope.launch {
                    dao.insert(TestInfo(Clock.System.now().epochSeconds.toString(), "test"))
                }
            }) {
                Text("Push")
            }
            testList.value.forEach {
                Text(it.name)
            }
        }
    }
}