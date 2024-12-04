package com.heyanle.easy_bangumi_cm.unifile.core.contract

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.heyanle.easy_bangumi_cm.unifile.safeCloseNonRuntime


/**
 * Created by heyanlin on 2024/12/4.
 */
object Contract {
    fun queryForString(
        context: Context, self: Uri, column: String,
        defaultValue: String?
    ): String? {
        val resolver = context.contentResolver

        var c: Cursor? = null
        try {
            c = resolver.query(self, arrayOf(column), null, null, null)
            return if (c != null && c.moveToFirst() && !c.isNull(0)) {
                c.getString(0)
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            return defaultValue
        } finally {
            c?.safeCloseNonRuntime()
        }
    }

    fun queryForInt(
        context: Context, self: Uri, column: String,
        defaultValue: Int
    ): Int {
        return queryForLong(context, self, column, defaultValue.toLong()).toInt()
    }

    fun queryForLong(
        context: Context, self: Uri, column: String,
        defaultValue: Long
    ): Long {
        val resolver = context.contentResolver

        var c: Cursor? = null
        try {
            c = resolver.query(self, arrayOf(column), null, null, null)
            return if (c != null && c.moveToFirst() && !c.isNull(0)) {
                c.getLong(0)
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            return defaultValue
        } finally {
            c?.safeCloseNonRuntime()
        }
    }
}