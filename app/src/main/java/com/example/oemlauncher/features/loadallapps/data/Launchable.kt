package com.example.oemlauncher.features.loadallapps.data

import android.content.ComponentName
import android.graphics.drawable.Drawable

data class Launchable(
    val label: String,
    val component: ComponentName,
    val icon: Drawable
)
