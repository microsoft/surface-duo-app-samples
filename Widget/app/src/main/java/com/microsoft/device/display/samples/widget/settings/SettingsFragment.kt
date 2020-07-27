/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */
package com.microsoft.device.display.samples.widget.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.microsoft.device.display.samples.widget.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}