package com.example.oemlauncher.features.loadallapps.helper
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import com.example.oemlauncher.R
import com.example.oemlauncher.features.loadallapps.data.Launchable
import com.example.oemlauncher.features.loadallapps.presentation.LoadAllAppsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ContextMenuHelper {

    fun showContextMenu(
        anchor: View,
        app: Launchable,
        viewModel: LoadAllAppsViewModel
    ) {
        val popupView = anchor.context.layoutInflater.inflate(R.layout.popup_app_options, null)
        val popup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popup.elevation = 12f
        popup.isOutsideTouchable = true
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // --- App Info ---
        popupView.findViewById<View>(R.id.option_app_info).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${app.component.packageName}")
            }
            anchor.context.startActivity(intent)
            popup.dismiss()
        }

        // --- Uninstall ---
        popupView.findViewById<View>(R.id.option_uninstall).setOnClickListener {
            val packageName = app.component.packageName

            MaterialAlertDialogBuilder(anchor.context)
                .setTitle("Uninstall App")
                .setMessage("Are you sure you want to uninstall ${app.label}?")
                .setPositiveButton("Uninstall") { dialog, _ ->
                    try {
                        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                            data = Uri.parse("package:$packageName")
                            putExtra(Intent.EXTRA_RETURN_RESULT, true)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        anchor.context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(anchor.context, "Cannot uninstall this app", Toast.LENGTH_SHORT).show()
                    }
                    popup.dismiss()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    popup.dismiss()
                    dialog.dismiss()
                }
                .show()
        }

        // --- Favorite ---
        popupView.findViewById<View>(R.id.option_fav).setOnClickListener {
            viewModel.toggleFavorite(app)
            popup.dismiss()
        }

        // --- Show popup ABOVE anchor ---
        popupView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        val popupHeight = popupView.measuredHeight

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        popup.showAtLocation(
            anchor,
            Gravity.NO_GRAVITY,
            anchorX,
            anchorY - popupHeight
        )
    }
}