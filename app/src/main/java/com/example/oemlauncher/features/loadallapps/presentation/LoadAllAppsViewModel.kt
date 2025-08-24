package com.example.oemlauncher.features.loadallapps.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oemlauncher.features.loadallapps.model.AppRepository
import com.example.oemlauncher.features.loadallapps.data.Launchable

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoadAllAppsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app.packageManager)
    private val prefs = app.getSharedPreferences("launcher", Application.MODE_PRIVATE)

    private val _allApps = MutableStateFlow<List<Launchable>>(emptyList())
    val allApps: StateFlow<List<Launchable>> = _allApps.asStateFlow()

    private val _query = MutableStateFlow("")
    val filteredApps: StateFlow<List<Launchable>> =
        combine(_allApps, _query) { apps, q ->
            if (q.isBlank()) apps
            else apps.filter { it.label.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _favorites = MutableStateFlow<List<Launchable>>(emptyList())
    val favorites: StateFlow<List<Launchable>> = _favorites.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            val apps = repo.loadAllApps()
            _allApps.value = apps
            reloadFavorites(apps)
        }
    }

    fun setQuery(q: String) {
        _query.value = q
    }

    fun toggleFavorite(app: Launchable) {
        val flat = "${app.component.packageName}/${app.component.className}"
        val saved = prefs.getStringSet("favorites", emptySet())!!.toMutableSet()
        if (saved.contains(flat)) saved.remove(flat) else saved.add(flat)
        prefs.edit().putStringSet("favorites", saved).apply()
        reloadFavorites(_allApps.value)
    }

    private fun reloadFavorites(apps: List<Launchable>) {
        val saved = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        val favs = saved.mapNotNull { flat ->
            val i = flat.indexOf('/')
            if (i <= 0) null
            else {
                val pkg = flat.substring(0, i)
                val cls = flat.substring(i + 1)
                apps.find { it.component.packageName == pkg && it.component.className == cls }
            }
        }
        _favorites.value = favs
    }
}