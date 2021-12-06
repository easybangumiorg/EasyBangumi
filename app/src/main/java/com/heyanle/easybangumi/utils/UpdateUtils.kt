package com.heyanle.easybangumi.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.heyanle.easybangumi.BuildConfig
import com.heyanle.easybangumi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2021/12/6 15:10.
 * https://github.com/heyanLE
 */
data class UpdateInfo(
    val name: String = "",
    val body: String = "",
    val downloadUrl: String = "",
    val publicTime: String = "",
    val size: Long = 0
)

object UpdateUtils {

    private const val RELEASE_URL = "https://api.github.com/repos/heyanLE/EasyBangumi/releases/latest"


    suspend fun check(activity: Activity, isShow: Boolean = false){
        check(WeakReference(activity), isShow)
    }

    suspend fun check(activity: WeakReference<Activity>, isShow: Boolean){
        val updateInfo = getLatestUpdate() ?: return
        val now = BuildConfig.VERSION_NAME.replace(".", "").toInt()
        val latest = updateInfo.name.replace(".", "").toInt()
        "$now -> $latest".logI("UpdateUtils")
        "$now -> $latest".toastWithDebug()
        withContext(Dispatchers.Main){
            val act = activity.get() ?: return@withContext
            if(latest > now){
                AlertDialog.Builder(act, if(DarkUtils.dark()) R.style.Theme_EasyBangumi_Toast_Dark else R.style.Theme_EasyBangumi_Toast_Light).setTitle(act.getString(R.string.new_version, updateInfo.name)).setMessage(updateInfo.body)
                    .setPositiveButton(R.string.goto_download){_, _ ->
                        act.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl)))
                    }.setNegativeButton(R.string.cancel, null).show()
            }else if(isShow){
                Toast.makeText(act, R.string.current_latest, Toast.LENGTH_SHORT).show()
            }
        }

    }


    private suspend fun getLatestUpdate(): UpdateInfo? {

        return withContext(Dispatchers.IO){
            runCatching {
                val body = OkHttpUtils.get(RELEASE_URL)
                val jo = JSONObject(body)
                UpdateInfo(
                    name = jo.getString("name"),
                    body = jo.getString("body"),
                    downloadUrl = jo.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"),
                    publicTime = jo.getString("published_at"),
                    size = jo.getJSONArray("assets").getJSONObject(0).getLong("size"),
                )
            }.getOrElse {
                it.printStackTrace()
                null
            }
        }

    }

}