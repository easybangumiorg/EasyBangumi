package com.heyanle.easybangumi.ui.common

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.heyanle.easybangumi.R

/**
 * Created by HeYanLe on 2023/1/7 18:44.
 * https://github.com/heyanLE
 */
@Composable
fun ErrorPage(
    modifier: Modifier = Modifier,
    image: Any = R.drawable.error_ikuyo,
    errorMsg: String = "",
    clickEnable: Boolean = false,
    other: @Composable ()->Unit = {},
    onClick: ()->Unit = {},

    ){
    WhitePage(
        modifier.let {
            if(clickEnable){
                it.clickable {
                    onClick()
                }
            }else {
                it
            }
        }, image, errorMsg, other)
}

@Composable
fun LoadingPage(
    modifier: Modifier = Modifier,
    loadingMsg: String = stringResource(id = R.string.loading),
    other: @Composable ()->Unit = {},
){
    WhitePage(modifier, Uri.parse("file:///android_asset/loading_ryo.gif"), loadingMsg, other)
}

@Composable
fun WhitePage(
    modifier: Modifier = Modifier,
    image: Any ,
    message: String = "",
    other: @Composable ()->Unit = {},
){
    Box(
        modifier = Modifier.then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = ImageRequest
                    .Builder(LocalContext.current)
                    .decoderFactory(
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            ImageDecoderDecoder.Factory()
                        else GifDecoder.Factory()
                    )
                    .crossfade(true)
                    .data(image).build(),
                contentDescription = message,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                color = Color.Gray.copy(alpha = 0.6f),
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            other()
        }
    }


}
