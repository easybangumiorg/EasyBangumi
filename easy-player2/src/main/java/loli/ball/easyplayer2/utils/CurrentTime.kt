package loli.ball.easyplayer2.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Date

/**
 * Returns the user's system-formatted time and updates only when the minute, timezone, locale or
 * explicit clock value changes. The receiver lives exactly as long as the calling composition.
 */
@Composable
fun rememberCurrentTimeText(): String {
    val context = LocalContext.current
    var text by remember(context) { mutableStateOf(formatCurrentTime(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                text = formatCurrentTime(context)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_LOCALE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    return text
}

internal fun formatCurrentTime(context: Context, date: Date = Date()): String {
    return DateFormat.getTimeFormat(context).format(date)
}
