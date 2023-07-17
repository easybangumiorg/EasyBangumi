package com.heyanle.easybangumi4.source.config

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.heyanle.bangumi_source_api.api.configuration.ConfigField
import com.heyanle.easybangumi4.APP
import java.io.File

/**
 * Created by HeYanLe on 2023/6/22 14:56.
 * https://github.com/heyanLE
 */
object ConfigMaster {


    private val configMap = hashMapOf<String, Pair<File, JsonObject>>()

    private fun getJsonObject(configField: ConfigField):  Pair<File, JsonObject>? {
        return runCatching {
            val file = getFile(configField)
            val target = File(file, "config.json")
            if(target.exists()){
                val t = target.readText()
                target to JsonParser.parseString(t).asJsonObject
            }else{
                target to JsonObject()
            }
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

    private fun getFile(configField: ConfigField): File{
        return File(APP.getExternalFilesDir("config")?:APP.cacheDir, configField.sourceKey)
    }



}