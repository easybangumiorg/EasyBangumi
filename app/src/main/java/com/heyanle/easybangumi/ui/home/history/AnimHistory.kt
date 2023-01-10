package com.heyanle.easybangumi.ui.home.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Created by HeYanLe on 2023/1/9 21:51.
 * https://github.com/heyanLE
 */
@Composable
fun AnimHistory() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()){
        items(50){
            Text(text = "test")
        }
    }
}