/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class CatapultApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Observe dynamic colors changes. The launcher is always dark, so use the dark overlay
        // instead of the default DayNight one, which follows the system UI mode.
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setThemeOverlay(com.google.android.material.R.style.ThemeOverlay_Material3_DynamicColors_Dark)
                .build()
        )
    }
}
