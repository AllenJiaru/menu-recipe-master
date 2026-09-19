package com.example.shiyu.ui.splash

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.data.repository.RecipeRepository
import com.example.shiyu.sync.AutoSyncManager
import com.example.shiyu.util.Constants
import com.example.shiyu.util.RoleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val roleManager: RoleManager,
    private val recipeRepository: RecipeRepository,
    private val backendRepository: BackendRepository,
    private val autoSyncManager: AutoSyncManager
) : ViewModel() {

    sealed class NavigationEvent {
        object NavigateToLogin : NavigationEvent()
        object NavigateToChefHome : NavigationEvent()
        object NavigateToDinerHome : NavigationEvent()
    }

    private val _navigationEvent = mutableStateOf<NavigationEvent?>(null)
    val navigationEvent: State<NavigationEvent?> = _navigationEvent

    init {
        checkAndNavigate()
    }

    private fun checkAndNavigate() {
        viewModelScope.launch {
            delay(500)
            backendRepository.autoLogin()
            val role = backendRepository.getSavedRole()
            when {
                !backendRepository.isLoggedIn -> {
                    _navigationEvent.value = NavigationEvent.NavigateToLogin
                }
                role == Constants.ROLE_CHEF -> {
                    roleManager.setCurrentRole(Constants.ROLE_CHEF)
                    autoSyncManager.trySync("app_startup")
                    _navigationEvent.value = NavigationEvent.NavigateToChefHome
                }
                role == Constants.ROLE_DINER -> {
                    roleManager.setCurrentRole(Constants.ROLE_DINER)
                    autoSyncManager.trySync("app_startup")
                    _navigationEvent.value = NavigationEvent.NavigateToDinerHome
                }
                else -> {
                    roleManager.setCurrentRole(Constants.ROLE_CHEF)
                    autoSyncManager.trySync("app_startup")
                    _navigationEvent.value = NavigationEvent.NavigateToChefHome
                }
            }
        }
    }
}
