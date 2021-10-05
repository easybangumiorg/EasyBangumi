package com.heyanle.easybangumi.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Created by HeYanLe on 2021/9/12 15:35.
 * https://github.com/heyanLE
 */

inline fun <reified T:Activity> Context.start (block: Intent.() -> Unit = {}){
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivity(intent)
}

inline fun Context.start (target: Class<in Activity>, block: Intent.() -> Unit = {}){
    val intent = Intent(this, target)
    intent.block()
    startActivity(intent)
}

inline fun <T: Activity> Context.start (activity: T, block: Intent.() -> Unit = {}){
    val intent = Intent(this, activity::class.java)
    intent.block()
    startActivity(intent)
}

inline fun <reified T:Activity> Activity.start (requestCode: Int, block: Intent.() -> Unit = {}){
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivityForResult(intent, requestCode)
}

inline fun <reified T:Activity> Fragment.start (requestCode: Int, block: Intent.() -> Unit = {}){
    val intent = Intent(this.requireContext(), T::class.java)
    intent.block()
    startActivityForResult(intent, requestCode)
}

