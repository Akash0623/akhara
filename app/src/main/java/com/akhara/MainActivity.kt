package com.akhara

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.akhara.security.AppLockManager
import com.akhara.ui.navigation.BottomNavBar
import com.akhara.ui.navigation.NavGraph
import com.akhara.ui.navigation.Routes
import com.akhara.ui.navigation.Screen
import com.akhara.ui.screens.lock.AppLockScreen
import com.akhara.ui.screens.splash.SplashScreen
import com.akhara.ui.theme.AkharaTheme
import com.akhara.ui.theme.BackgroundDark

class MainActivity : FragmentActivity() {

    private lateinit var lockManager: AppLockManager
    private var isUnlocked by mutableStateOf(false)
    private var lockError by mutableStateOf<String?>(null)
    private var showSplash by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        lockManager = AppLockManager(this)
        val app = application as AkharaApp

        if (!lockManager.isLockEnabled()) {
            isUnlocked = true
        }

        setContent {
            AkharaTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = !showSplash,
                        enter = fadeIn(androidx.compose.animation.core.tween(400)),
                        exit = fadeOut()
                    ) {
                        if (!isUnlocked && lockManager.isLockEnabled()) {
                            AppLockScreen(
                                onUnlockClick = { promptBiometric() },
                                errorMessage = lockError
                            )
                        } else {
                            val navController = rememberNavController()
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            val showBottomBar = currentRoute in listOf(
                                Screen.Home.route,
                                Screen.Calendar.route,
                                Screen.Library.route,
                                Screen.Stats.route
                            )

                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = BackgroundDark,
                                bottomBar = {
                                    if (showBottomBar) {
                                        BottomNavBar(
                                            currentRoute = currentRoute,
                                            onTabSelected = { screen ->
                                                navController.navigate(screen.route) {
                                                    popUpTo(Screen.Home.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            onLogWorkout = {
                                                navController.navigate(Routes.LOG_WORKOUT)
                                            }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    NavGraph(
                                        navController = navController,
                                        repository = app.repository
                                    )
                                }
                            }
                        }
                    }

                    if (showSplash) {
                        SplashScreen(onFinished = { showSplash = false })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (lockManager.isLockEnabled() && !isUnlocked) {
            promptBiometric()
        }
    }

    private fun promptBiometric() {
        lockError = null
        lockManager.authenticate(
            activity = this,
            onSuccess = {
                isUnlocked = true
                lockError = null
            },
            onFailure = { error ->
                lockError = error
            }
        )
    }

    override fun onStop() {
        super.onStop()
        if (lockManager.isLockEnabled()) {
            isUnlocked = false
        }
    }
}
