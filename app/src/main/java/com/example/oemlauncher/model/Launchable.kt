package com.example.oemlauncher.model

import android.content.ComponentName
import android.graphics.drawable.Drawable

data class Launchable(
    val label: String,
    val component: ComponentName,
    val icon: Drawable
)
