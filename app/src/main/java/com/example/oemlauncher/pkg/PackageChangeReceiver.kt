package com.example.oemlauncher.pkg


import android.content.IntentFilter

object PackageChangeReceiver {
    val intentFilter: IntentFilter by lazy {
        IntentFilter().apply {
            addAction("android.intent.action.PACKAGE_ADDED")
            addAction("android.intent.action.PACKAGE_REMOVED")
            addAction("android.intent.action.PACKAGE_CHANGED")
            addDataScheme("package")
        }
    }
}