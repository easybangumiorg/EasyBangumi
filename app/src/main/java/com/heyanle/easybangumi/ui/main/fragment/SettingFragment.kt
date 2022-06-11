package com.heyanle.easybangumi.ui.main.fragment

import android.os.Build
import android.os.Bundle
import androidx.core.os.persistableBundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.heyanle.easybangumi.BuildConfig
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.utils.DarkUtils
import com.heyanle.easybangumi.utils.UpdateUtils
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2021/9/20 15:55.
 * https://github.com/heyanLE
 */
class SettingFragment: PreferenceFragmentCompat(){


    override fun onResume() {
        super.onResume()

        preferenceManager.findPreference<SwitchPreferenceCompat>("auto_dark")?.isChecked = DarkUtils.autoDark()
        //Toast.makeText(EasyApplication.INSTANCE, "SettingFragmentOnResume", Toast.LENGTH_SHORT).show()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
        preferenceManager.findPreference<SwitchPreferenceCompat>("auto_dark")?.let{
            it.setOnPreferenceChangeListener { _, newValue ->
                val activity = activity?: return@setOnPreferenceChangeListener false
                if (DarkUtils.autoDark() == newValue){
                    return@setOnPreferenceChangeListener false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    DarkUtils.autoDark(newValue as Boolean, activity)
                }else{
                    return@setOnPreferenceChangeListener !(newValue as Boolean)
                }
                true
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                it.isEnabled = false
            }
        }

        preferenceManager.findPreference<Preference>("current_version")?.let {
            it.title = getString(R.string.current_version, BuildConfig.VERSION_NAME)
        }
        preferenceManager.findPreference<Preference>("check_update")?.let {
            it.setOnPreferenceClickListener {
                lifecycleScope.launch {
                    runCatching {
                        UpdateUtils.check(requireActivity(),true)
                    }
                }
                true
            }
        }


    }
}