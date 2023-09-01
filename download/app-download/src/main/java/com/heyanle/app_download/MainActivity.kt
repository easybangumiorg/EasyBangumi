package com.heyanle.app_download

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arialyy.aria.core.Aria
import java.io.File
import java.net.URLEncoder

/**
 * Created by heyanlin on 2023/8/29.
 */
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Aria.init(this)
        setContent {
            MaterialTheme(
                //colorScheme =  dynamicLightColorScheme(this)
            ) {

                var text by remember {
                    mutableStateOf("")
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = text,
                            onValueChange = { text = it })
                        TextButton(onClick = {
                            DownloadController.newTask(
                                url = text,
                                type = 1,
                                folder = getExternalFilesDir("test")?.absolutePath ?: "",
                                name = URLEncoder.encode(text, "utf-8")
                            )
                        }) {
                            Text(text = "下载")
                        }
                    }
                    TextButton(onClick = {
                        DownloadController.change(File(getExternalFilesDir("test"), "test.m3u8").absolutePath, File(getExternalFilesDir("test"), "test.mp4").absolutePath)
                    }) {
                        Text(text = "test")
                    }

                    val items = DownloadController.flow.collectAsState()
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ){
                        items(items.value){
                            val info = DownloadBus.getInfo(it.url)
                            Column {
                                Text(text = it.url)
                                Text(text = info.process.value)
                                Text(text = info.speed.value)
                                Text(text = info.status.value.toString())
                            }
                        }
                    }
                }
            }

            
        }
    }
}