package com.example.oemlauncher.ui


import android.app.role.RoleManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oemlauncher.data.AppRepository
import com.example.oemlauncher.databinding.ActivityMainBinding
import com.example.oemlauncher.model.Launchable
import com.example.oemlauncher.pkg.PackageChangeReceiver

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repo: AppRepository
    private lateinit var allAdapter: AppAdapter
    private lateinit var favAdapter: AppAdapter

    private val prefs by lazy { getSharedPreferences("launcher", Context.MODE_PRIVATE) }

    private var allApps: List<Launchable> = emptyList()
    private var filteredApps: List<Launchable> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request HOME role if needed (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            if (rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME), 1001)
            }
        }

        repo = AppRepository(packageManager)

        // Favorites row (horizontal)
        favAdapter = AppAdapter(this, emptyList())
        binding.favorites.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = favAdapter
        }

        // All apps grid
        allAdapter = AppAdapter(this, emptyList(), onLongPress = { app, anchor ->
            showContextMenu(app, anchor)
        })

        val span = 4 // tweak for tablets
        binding.apps.apply {
            layoutManager = GridLayoutManager(this@MainActivity, span)
            adapter = allAdapter
        }

        // Search filter
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterApps(s?.toString().orEmpty()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Initial load
        loadApps()

        // Listen for package changes
        registerReceiver(packageReceiver, PackageChangeReceiver.intentFilter)
    }

    private fun loadApps() {
        allApps = repo.loadAllApps()
        filteredApps = allApps
        allAdapter.submitList(filteredApps)
        reloadFavorites()
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase()
        filteredApps = if (q.isEmpty()) allApps
        else allApps.filter { it.label.lowercase().contains(q) }
        allAdapter.submitList(filteredApps)
    }

    private fun reloadFavorites() {
        val saved = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        val favs = saved.mapNotNull { flat ->
            val i = flat.indexOf('/')
            if (i <= 0) null
            else {
                val pkg = flat.substring(0, i)
                val cls = flat.substring(i + 1)
                allApps.find { it.component.packageName == pkg && it.component.className == cls }
            }
        }
        favAdapter.submitList(favs)
    }

    private fun showContextMenu(app: Launchable, anchor: android.view.View) {
        val popup = android.widget.PopupMenu(this, anchor)
        val flat = "${app.component.packageName}/${app.component.className}"
        val saved = prefs.getStringSet("favorites", emptySet())!!.toMutableSet()
        val isFav = saved.contains(flat)
        popup.menu.add(if (isFav) "Remove from favorites" else "Add to favorites")
        popup.setOnMenuItemClickListener {
            if (isFav) saved.remove(flat) else saved.add(flat)
            prefs.edit().putStringSet("favorites", saved).apply()
            reloadFavorites()
            true
        }
        popup.show()
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Light debounce could be added; for MVP just reload
            loadApps()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageReceiver)
    }
}