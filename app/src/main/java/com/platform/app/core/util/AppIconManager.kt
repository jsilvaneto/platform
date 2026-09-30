package com.platform.app.core.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object AppIconManager {
    const val ICON_CLASSIC = "classic"
    const val ICON_MODERN = "modern"

    private const val ALIAS_CLASSIC = "com.platform.app.MainActivityClassic"
    private const val ALIAS_MODERN = "com.platform.app.MainActivityModern"

    fun applyIcon(context: Context, iconKey: String) {
        try {
            val pm = context.packageManager
            val isClassic = iconKey == ICON_CLASSIC

            val classicComponent = ComponentName(context, ALIAS_CLASSIC)
            val modernComponent = ComponentName(context, ALIAS_MODERN)

            pm.setComponentEnabledSetting(
                classicComponent,
                if (isClassic) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            pm.setComponentEnabledSetting(
                modernComponent,
                if (!isClassic) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (_: Exception) {
            // Silently fallback if running in test environment or launcher restrictions
        }
    }
}
