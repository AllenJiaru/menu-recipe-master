package com.example.shiyu.sync

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.example.shiyu.api.ApiClient
import com.example.shiyu.data.repository.BackendRepository
import kotlinx.coroutines.*

class AutoSyncManager(
    private val context: Context,
    private val backendRepository: BackendRepository
) {
    private val syncManager = SyncManager(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var periodicJob: Job? = null
    private var isSyncing = false
    private var lastSyncAttempt = 0L
    private var startedActivities = 0

    companion object {
        private const val PERIODIC_INTERVAL_MS = 5 * 60 * 1000L
        private const val MIN_SYNC_INTERVAL_MS = 30 * 1000L
    }

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityStarted(activity: Activity) {
            startedActivities++
            if (startedActivities == 1) {
                trySync("app_foreground")
            }
        }

        override fun onActivityStopped(activity: Activity) {
            startedActivities--
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    fun start() {
        val app = context.applicationContext as? Application
        app?.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        startPeriodicSync()
    }

    fun stop() {
        val app = context.applicationContext as? Application
        app?.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        periodicJob?.cancel()
    }

    private fun startPeriodicSync() {
        periodicJob?.cancel()
        periodicJob = scope.launch {
            while (isActive) {
                delay(PERIODIC_INTERVAL_MS)
                trySync("periodic")
            }
        }
    }

    fun trySync(trigger: String) {
        if (isSyncing) return
        if (!backendRepository.isLoggedIn) return

        val now = System.currentTimeMillis()
        if (now - lastSyncAttempt < MIN_SYNC_INTERVAL_MS) return

        lastSyncAttempt = now
        isSyncing = true

        scope.launch {
            try {
                val token = context.getSharedPreferences("backend_prefs", Context.MODE_PRIVATE)
                    .getString("token", null)
                if (token.isNullOrEmpty()) {
                    isSyncing = false
                    return@launch
                }
                ApiClient.setToken(token)
                syncManager.fullSync()
            } catch (_: Exception) {
            } finally {
                isSyncing = false
            }
        }
    }

    fun syncNow(callback: ((Boolean, String) -> Unit)? = null) {
        if (isSyncing) {
            callback?.invoke(false, "正在同步中...")
            return
        }
        if (!backendRepository.isLoggedIn) {
            callback?.invoke(false, "未登录")
            return
        }

        isSyncing = true
        lastSyncAttempt = System.currentTimeMillis()

        scope.launch {
            try {
                val token = context.getSharedPreferences("backend_prefs", Context.MODE_PRIVATE)
                    .getString("token", null)
                if (token.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        callback?.invoke(false, "未登录")
                    }
                    isSyncing = false
                    return@launch
                }
                ApiClient.setToken(token)
                val result = syncManager.fullSync()
                withContext(Dispatchers.Main) {
                    callback?.invoke(result.success, result.message)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback?.invoke(false, e.message ?: "同步失败")
                }
            } finally {
                isSyncing = false
            }
        }
    }
}
