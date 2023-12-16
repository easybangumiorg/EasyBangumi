package com.heyanle.easybangumi4.source.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.heyanle.easybangumi4.source_api.utils.api.CaptchaHelper
import com.heyanle.easybangumi4.ui.common.dismiss
import com.heyanle.easybangumi4.ui.common.moeDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by LoliBall on 2023/12/17 1:02.
 * https://github.com/WhichWho
 */
object CaptchaHelperImpl : CaptchaHelper {

    override suspend fun start(image: Any, text: String?, title: String?, hint: String?): String {
        return withContext(Dispatchers.Main) {
            suspendCoroutine { con ->
                start(image, text, title, hint) {
                    con.resume(it)
                }
            }
        }
    }

    override fun start(
        image: Any,
        text: String?,
        title: String?,
        hint: String?,
        onFinish: (String) -> Unit
    ) {
        "".moeDialog { self ->
            Surface {
                Column {
                    title?.let { Text(text = it, style = MaterialTheme.typography.titleMedium) }
                    text?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                    AsyncImage(
                        model = image,
                        contentDescription = "captcha",
                        modifier = Modifier.fillMaxWidth()
                    )
                    var input by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = hint?.let { { Text(text = hint) } }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { self.dismiss() }) {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                        }
                        TextButton(onClick = {
                            self.dismiss()
                            onFinish(input)
                        }) {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.confirm))
                        }
                    }
                }
            }
        }
    }

}