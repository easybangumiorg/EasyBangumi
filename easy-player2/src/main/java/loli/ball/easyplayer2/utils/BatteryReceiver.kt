package loli.ball.easyplayer2.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.BatteryManager.EXTRA_LEVEL
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


/**
 * Created by HeYanLe on 2023/6/13 22:27.
 * https://github.com/heyanLE
 */

@Composable
fun rememberBatteryReceiver(): BatteryReceiver{
    val rec = remember {
        BatteryReceiver()
    }
    val ctx = LocalContext.current
    DisposableEffect(key1 = Unit){
        rec.register(ctx)
        onDispose {
            rec.unregister(ctx)
        }
    }
    return rec
}
class BatteryReceiver: BroadcastReceiver() {

    var electricity = mutableStateOf(100)
    var isCharge = mutableStateOf(false)


    fun register(ctx: Context){
        ctx.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun unregister(ctx: Context){
        ctx.unregisterReceiver(this)
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent!!.extras ?: return
        val current = extras.getInt(BatteryManager.EXTRA_LEVEL) // 获得当前电量

        val total = extras.getInt(BatteryManager.EXTRA_SCALE) // 获得总电量

        val status: Int = extras.getInt(BatteryManager.EXTRA_STATUS)
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING


        val percent = current * 100 / total
        electricity.value = percent
        isCharge.value = isCharging
    }
}