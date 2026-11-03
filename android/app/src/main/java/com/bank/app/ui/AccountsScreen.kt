package com.bank.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bank.app.data.Account
import com.bank.app.data.ApiClient
import kotlinx.coroutines.launch
import java.util.Locale

class AccountsViewModel : ViewModel() {
    var accounts by mutableStateOf<List<Account>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    fun load() {
        isLoading = true
        error = null
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getAccounts()
                when {
                    response.isSuccessful -> accounts = response.body() ?: emptyList()
                    response.code() == 401 -> sessionExpired = true
                    else -> error = "Failed to load accounts (${response.code()})"
                }
            } catch (e: Exception) {
                error = "Cannot reach server"
            } finally {
                isLoading = false
            }
        }
    }
}

fun formatBalance(balance: Float): String = String.format(Locale.US, "%,.3f KWD", balance)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onAccountClick: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AccountsViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(viewModel.sessionExpired) {
        if (viewModel.sessionExpired) onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Accounts") },
                actions = { TextButton(onClick = onLogout) { Text("Logout") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val error = viewModel.error
            when {
                viewModel.isLoading && viewModel.accounts.isEmpty() ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                error != null ->
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Retry") }
                    }

                viewModel.accounts.isEmpty() ->
                    Text("No accounts found", Modifier.align(Alignment.Center))

                else ->
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.accounts) { account ->
                            AccountCard(account) { onAccountClick(account.accountNumber) }
                        }
                    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCard(account: Account, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(account.nickname ?: "Account", style = MaterialTheme.typography.titleMedium)
            Text(account.accountNumber, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Text(formatBalance(account.balance), style = MaterialTheme.typography.headlineSmall)
        }
    }
}
