package com.example.oemlauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oemlauncher.R
import com.example.oemlauncher.databinding.ActivityMainBinding
import com.example.oemlauncher.features.loadallapps.adapter.AppAdapter
import com.example.oemlauncher.features.loadallapps.data.Launchable
import com.example.oemlauncher.features.loadallapps.presentation.AllAppsBottomSheetFragment
import com.example.oemlauncher.features.loadallapps.presentation.LoadAllAppsViewModel
import com.example.oemlauncher.pkg.PackageChangeReceiver
import kotlinx.coroutines.flow.collectLatest

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {


    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: LoadAllAppsViewModel
    private lateinit var favAdapter: AppAdapter

    private lateinit var gestureDetector: GestureDetector
    private var allAppsSheet: AllAppsBottomSheetFragment? = null

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.loadApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            viewModel = ViewModelProvider(this)[LoadAllAppsViewModel::class.java]
            gestureDetector = GestureDetector(this, this)

            setupUI()
            observeVM()
            registerReceiver(packageReceiver, PackageChangeReceiver.intentFilter)

            // Gesture overlay for smooth swipe detection anywhere
            val gestureOverlay = findViewById<View>(R.id.gestureOverlay)
            gestureOverlay.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun setupUI() {
        favAdapter = AppAdapter(this, emptyList())
        binding.favorites.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = favAdapter
        }
    }

    private fun observeVM() {
        lifecycleScope.launchWhenStarted {
            viewModel.favorites.collectLatest { favAdapter.submitList(it) }
        }
    }

    // --- Gesture callbacks ---
    override fun onDown(e: MotionEvent): Boolean = true
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false // not needed anymore
    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null || e2 == null) return false
        val deltaY = e2.y - e1.y

        // Swipe up → show all apps
        if (deltaY < -150 && velocityY < 0) {
            if (allAppsSheet == null || allAppsSheet?.isVisible == false) {
                allAppsSheet = AllAppsBottomSheetFragment()
                allAppsSheet?.show(supportFragmentManager, "all_apps")
            }
            return true
        }

        // Swipe down → close all apps
        if (deltaY > 150 && velocityY > 0) {
            allAppsSheet?.dismiss()
            return true
        }

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageReceiver)
    }
}
