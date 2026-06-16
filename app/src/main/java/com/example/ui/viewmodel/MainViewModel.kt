package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AssetResponse
import com.example.data.api.SignalResponse
import com.example.data.repository.SessionManager
import com.example.data.repository.TradingRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = TradingRepository(sessionManager)

    // Device Properties
    val deviceId: String = sessionManager.deviceId

    // Navigation and Activation States
    private val _isSplashLoading = MutableStateFlow(true)
    val isSplashLoading: StateFlow<Boolean> = _isSplashLoading.asStateFlow()

    private val _isActivated = MutableStateFlow(sessionManager.isActivated)
    val isActivated: StateFlow<Boolean> = _isActivated.asStateFlow()

    private val _vipPlan = MutableStateFlow(sessionManager.activationPlan)
    val vipPlan: StateFlow<String> = _vipPlan.asStateFlow()

    // Key Activation Text/Error
    private val _activationKey = MutableStateFlow("")
    val activationKey: StateFlow<String> = _activationKey.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    private val _activationError = MutableStateFlow<String?>(null)
    val activationError: StateFlow<String?> = _activationError.asStateFlow()

    // Signals and Assets Data State
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _signals = MutableStateFlow<List<SignalResponse>>(emptyList())
    val signals: StateFlow<List<SignalResponse>> = _signals.asStateFlow()

    private val _assets = MutableStateFlow<List<AssetResponse>>(emptyList())
    val assets: StateFlow<List<AssetResponse>> = _assets.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(sessionManager.getFavorites())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // Global Interactive Navigation and Chart Preview state
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _selectedChartAsset = MutableStateFlow<AssetResponse?>(null)
    val selectedChartAsset: StateFlow<AssetResponse?> = _selectedChartAsset.asStateFlow()

    fun selectChartAsset(asset: AssetResponse) {
        _selectedChartAsset.value = asset
    }

    fun setTab(index: Int) {
        _activeTab.value = index
    }

    fun navigateToChartByPair(pair: String) {
        val foundAsset = _assets.value.firstOrNull { it.name.lowercase() == pair.lowercase() }
        if (foundAsset != null) {
            _selectedChartAsset.value = foundAsset
        } else {
            _selectedChartAsset.value = AssetResponse(name = pair, category = "OTC")
        }
        _activeTab.value = 1 // Switch to Chart tab (index 1)
    }

    fun navigateToChart(asset: AssetResponse) {
        _selectedChartAsset.value = asset
        _activeTab.value = 1 // Switch to Chart tab (index 1)
    }

    // UI Search & Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Server Configuration state
    private val _apiUrl = MutableStateFlow(sessionManager.apiUrl)
    val apiUrl: StateFlow<String> = _apiUrl.asStateFlow()

    private val _isDemoMode = MutableStateFlow(sessionManager.isDemoModeMode)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(sessionManager.appLanguage)
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private var lastNotifiedSignalId: String? = null

    private fun updateSignals(list: List<SignalResponse>) {
        if (_signals.value != list) {
            _signals.value = list
            if (list.isNotEmpty()) {
                val signal = list.first()
                val signalId = "${signal.pair}_${signal.direction}_${signal.price}"
                if (lastNotifiedSignalId != signalId) {
                    lastNotifiedSignalId = signalId
                    NotificationHelper.showSignalNotification(
                        getApplication(),
                        pair = signal.pair,
                        direction = signal.direction,
                        price = signal.price,
                        duration = signal.duration,
                        confidence = signal.confidence
                    )
                }
            }
        }
    }

    init {
        // Create Android Notification Channels
        NotificationHelper.createNotificationChannel(application)
        
        // Start live Quotex index prices synchronization loop
        com.example.util.PriceSyncHelper.startSyncLoop()
        
        // Execute Splash verification sequence
        viewModelScope.launch {
            delay(2200) // Beautiful splash dwell time
            _isSplashLoading.value = false
        }

        // Auto-load signals and assets on launch
        loadTradingData()

        // Background automatic live synchronization loop
        viewModelScope.launch {
            while (true) {
                delay(1200) // Poll every 1.2s to stay responsive but light on resources
                val signalsResponse = repository.getSignals()
                signalsResponse.onSuccess { list ->
                    updateSignals(list)
                }
            }
        }
    }

    fun toggleLanguage() {
        val newLang = if (_appLanguage.value == "ar") "en" else "ar"
        sessionManager.appLanguage = newLang
        _appLanguage.value = newLang
    }

    fun setActivationKey(key: String) {
        _activationKey.value = key
        _activationError.value = null
    }

    fun setApiUrl(url: String) {
        _apiUrl.value = url
        sessionManager.apiUrl = url
    }

    fun toggleDemoMode(active: Boolean) {
        _isDemoMode.value = active
        sessionManager.isDemoModeMode = active
        loadTradingData()
    }

    fun validateActivationKey() {
        val key = _activationKey.value.trim()
        if (key.isEmpty()) {
            _activationError.value = "err_empty_key"
            return
        }

        viewModelScope.launch {
            _isValidating.value = true
            _activationError.value = null
            
            // Validate against Server or simulated rules in Repository
            val result = repository.validateKey(key)
            
            result.onSuccess { response ->
                when (response.status) {
                    "valid" -> {
                        val plan = response.plan ?: "pro"
                        sessionManager.isActivated = true
                        sessionManager.activationPlan = plan
                        _isActivated.value = true
                        _vipPlan.value = plan
                        _activationError.value = null
                        // Refresh data
                        loadTradingData()
                    }
                    "invalid" -> {
                        _activationError.value = "err_invalid_key"
                    }
                    "expired" -> {
                        _activationError.value = "err_expired_key"
                    }
                    "device_mismatch" -> {
                        _activationError.value = "err_device_mismatch"
                    }
                    else -> {
                        _activationError.value = "err_mismatch"
                    }
                }
            }.onFailure {
                _activationError.value = "err_connection_failed"
            }
            _isValidating.value = false
        }
    }

    fun loadTradingData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            val signalsResponse = repository.getSignals()
            val assetsResponse = repository.getAssets()

            signalsResponse.onSuccess { list ->
                updateSignals(list)
            }

            assetsResponse.onSuccess { list ->
                _assets.value = list
            }

            _isRefreshing.value = false
        }
    }

    fun togglePairFavorite(pair: String) {
        sessionManager.toggleFavorite(pair)
        _favorites.value = sessionManager.getFavorites()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun triggerRandomNotification() {
        val currentSignals = _signals.value
        if (currentSignals.isNotEmpty()) {
            val signal = currentSignals.random()
            NotificationHelper.showSignalNotification(
                getApplication(),
                pair = signal.pair,
                direction = signal.direction,
                price = signal.price,
                duration = signal.duration,
                confidence = signal.confidence
            )
        } else {
            NotificationHelper.showSignalNotification(
                getApplication(),
                pair = "EUR/USD",
                direction = "BUY",
                price = 1.08320,
                duration = 10,
                confidence = 72
            )
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _isActivated.value = false
        _vipPlan.value = ""
        _activationKey.value = ""
        _activationError.value = null
    }
}
