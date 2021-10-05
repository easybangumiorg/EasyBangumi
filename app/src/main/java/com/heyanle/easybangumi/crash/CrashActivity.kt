package com.heyanle.easybangumi.crash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ActivityCrashBinding

import com.heyanle.easybangumi.utils.start

/**
 * Created by HeYanLe on 2021/9/12 15:31.
 * https://github.com/heyanLE
 */
class CrashActivity : AppCompatActivity() {

    companion object{
        const val INTENT_KEY = "INTENT_KEY"
    }

    private val binding: ActivityCrashBinding by lazy {
        ActivityCrashBinding.inflate(LayoutInflater.from(this@CrashActivity))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    private fun initView(){
        val error: String = intent?.extras?.getString(INTENT_KEY) ?: return

        val dt = android.os.Build.MODEL
        val av = android.os.Build.VERSION.RELEASE

        binding.textDeviceType.text = getString(R.string.device_type, dt)
        binding.textAndroidVersion.text = getString(R.string.android_version, av)
        binding.textError.text = error


    }

}