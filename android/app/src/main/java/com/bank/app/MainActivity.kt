package com.bank.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bank.app.data.SessionManager
import com.bank.app.ui.AccountDetailsScreen
import com.bank.app.ui.AccountsScreen
import com.bank.app.ui.LoginScreen
import com.bank.app.ui.theme.BankAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BankAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BankNavHost()
                }
            }
        }
    }
}

@Composable
fun BankNavHost() {
    val navController = rememberNavController()

    fun logout() {
        SessionManager.clear()
        navController.navigate("login") {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("accounts") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("accounts") {
            AccountsScreen(
                onAccountClick = { navController.navigate("account/$it") },
                onLogout = { logout() }
            )
        }
        composable("account/{accountNumber}") { entry ->
            val accountNumber = entry.arguments?.getString("accountNumber") ?: ""
            AccountDetailsScreen(
                accountNumber = accountNumber,
                onBack = { navController.popBackStack() },
                onSessionExpired = { logout() }
            )
        }
    }
}
