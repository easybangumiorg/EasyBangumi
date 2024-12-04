package com.heyanle.easy_bangumi_cm.unifile

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.heyanle.easy_bangumi_cm.unifile.core.*
import com.heyanle.easy_bangumi_cm.unifile.core.contract.DocumentsContractApi19
import com.heyanle.easy_bangumi_cm.unifile.core.contract.DocumentsContractApi21
import java.io.File
import java.net.URI


/**
 * Created by heyanlin on 2024/12/4.
 */
val ASSET_PATH_PREFIX_LENGTH
    get() = "/android_asset/".length

actual object UniFile {

    fun fromUri(u: Uri, app: Context? = null) : IUniFile? {
        if (u.isFile() && ! u.isAssetUri() && !u.path.isNullOrEmpty()) {
            return RawFile(null, File(u.path?:""))
        }
        if(app == null){
            Log.e("UniFile", "UniFile need init first")
            return null
        }

        if (u.isAssetUri()) {
            val originPath = u.path?.substring(ASSET_PATH_PREFIX_LENGTH)?:""
            val path = UniUtils.normalize(originPath);
            return AssetsFile(null, app.assets, path)
        }

        if (u.isDocumentUri(app)) {
            if (u.isTreeDocumentUri(app)) {
                return TreeDocumentFile(null, app, DocumentsContractApi21.prepareTreeUri(u))
            } else {
                return SingleDocumentFile(null, app, u)
            }
        } else if (u.isMediaUri()){
            return MediaFile( app, u)
        }
        return null
    }

    fun fromUri(uri: String, app: Context? = null): IUniFile? {
        return fromUri(Uri.parse(uri), app)
    }

    actual fun fromFile(file: File): IUniFile {
        return RawFile(null, file)
    }

    actual fun fromUri(uri: URI): IUniFile? {
        if (uri.scheme != "file") {
            return null
        }
        return RawFile(null, File(uri))
    }

    fun fromAssets(path: String, context: Context): IUniFile? {
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_FILE)
            .authority("")
            .path("android_asset/$path")
            .build()
        return fromUri(uri, context)
    }
}

private fun Uri.isFile(): Boolean {
    return scheme == "file"
}

private fun Uri.isDocumentUri(context: Context): Boolean {
    return DocumentsContractApi19.isDocumentUri(context, this);
}

private fun Uri.isTreeDocumentUri(context: Context): Boolean {
    return DocumentsContractApi21.isTreeDocumentUri(context, this);
}

private fun Uri.isAssetUri(): Boolean {
    return ContentResolver.SCHEME_FILE == scheme
            && pathSegments.size >= 2 && "android_asset" == pathSegments[0]
}

private fun Uri.isMediaUri(): Boolean {
    return ContentResolver.SCHEME_CONTENT == scheme
}

