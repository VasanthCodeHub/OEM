package com.example.oemlauncher.data

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import com.example.oemlauncher.model.Launchable
import java.text.Collator
import java.util.Locale

class AppRepository(private val pm: PackageManager) {
    fun loadAllApps(): List<Launchable> {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val results = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val collator = Collator.getInstance(Locale.getDefault())
        return results.map { ri ->
            val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.name
            Launchable(
                label = label,
                component = ComponentName(ri.activityInfo.packageName, ri.activityInfo.name),
                icon = ri.activityInfo.loadIcon(pm)
            )
        }.sortedWith { a, b -> collator.compare(a.label, b.label) }
    }
}