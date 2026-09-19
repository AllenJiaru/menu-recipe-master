package com.example.shiyu.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFS_NAME)

@Singleton
class RoleManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val CURRENT_ROLE = stringPreferencesKey(Constants.KEY_CURRENT_ROLE)
    }

    val currentRole: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_ROLE] ?: ""
    }

    suspend fun setCurrentRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_ROLE] = role
        }
    }

    suspend fun clearRole() {
        context.dataStore.edit { preferences ->
            preferences.remove(CURRENT_ROLE)
        }
    }

    fun isChef(role: String): Boolean = role == Constants.ROLE_CHEF
    fun isDiner(role: String): Boolean = role == Constants.ROLE_DINER

    fun getRoleDisplayName(role: String): String = when (role) {
        Constants.ROLE_CHEF -> "主厨"
        Constants.ROLE_DINER -> "食客"
        else -> "未知"
    }
}
