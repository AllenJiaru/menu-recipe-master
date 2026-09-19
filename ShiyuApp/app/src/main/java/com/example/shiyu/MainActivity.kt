package com.example.shiyu

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.example.shiyu.sync.AutoSyncManager
import com.example.shiyu.ui.navigation.AppNavigation
import com.example.shiyu.ui.theme.LocalThemeManager
import com.example.shiyu.ui.theme.ShiyuTheme
import com.example.shiyu.ui.theme.ThemeManager
import com.example.shiyu.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var autoSyncManager: AutoSyncManager

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getSavedLanguage(newBase)
        val localizedContext = LocaleHelper.updateLocale(newBase, languageCode)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        autoSyncManager.start()
        val themeManager = ThemeManager(applicationContext)
        setContent {
            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                ShiyuTheme {
                    AppNavigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            autoSyncManager.stop()
        }
    }
}
