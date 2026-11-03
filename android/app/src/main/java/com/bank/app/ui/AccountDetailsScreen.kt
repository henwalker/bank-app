package com.bank.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bank.app.data.Account
import com.bank.app.data.ApiClient
import com.bank.app.data.NicknameRequest
import com.bank.app.data.SessionManager
import kotlinx.coroutines.launch

class AccountDetailsViewModel : ViewModel() {
    var account by mutableStateOf<Account?>(null)
        private set
    var nicknameInput by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    fun load(accountNumber: String) {
        isLoading = true
        error = null
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getAccount(accountNumber)
                when {
                    response.isSuccessful -> {
                        account = response.body()
                        nicknameInput = account?.nickname ?: ""
                    }
                    response.code() == 401 -> sessionExpired = true
                    response.code() == 404 -> error = "Account not found"
                    else -> error = "Failed to load account (${response.code()})"
                }
            } catch (e: Exception) {
                error = "Cannot reach server"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveNickname() {
        val current = account ?: return
        val trimmed = nicknameInput.trim()
        if (trimmed.length > 255) {
            error = "Nickname must be 255 characters or less"
            return
        }
        isSaving = true
        error = null
        message = null
        viewModelScope.launch {
            try {
                val response = ApiClient.api.updateNickname(
                    current.accountNumber,
                    NicknameRequest(trimmed.ifEmpty { null })
                )
                when {
                    response.isSuccessful -> {
                        account = response.body()
                        message = "Nickname saved"
                    }
                    response.code() == 401 -> sessionExpired = true
                    else -> error = "Failed to save nickname (${response.code()})"
                }
            } catch (e: Exception) {
                error = "Cannot reach server"
            } finally {
                isSaving = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    accountNumber: String,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: AccountDetailsViewModel = viewModel()
) {
    LaunchedEffect(accountNumber) { viewModel.load(accountNumber) }
    LaunchedEffect(viewModel.sessionExpired) {
        if (viewModel.sessionExpired) onSessionExpired()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Details") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val account = viewModel.account
            when {
                viewModel.isLoading -> CircularProgressIndicator()
                account == null -> Text(viewModel.error ?: "Account not found")
                else -> {
                    DetailRow("Account Number", account.accountNumber)
                    DetailRow("Customer Number", SessionManager.customerNumber?.toString() ?: "-")
                    DetailRow("Balance", formatBalance(account.balance))
                    DetailRow("Nickname", account.nickname ?: "Not set")

                    Spacer(Modifier.height(24.dp))
                    Text("Set nickname", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.nicknameInput,
                        onValueChange = { if (it.length <= 255) viewModel.nicknameInput = it },
                        label = { Text("Nickname") },
                        singleLine = true,
                        supportingText = { Text("${viewModel.nicknameInput.length}/255") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.saveNickname() },
                        enabled = !viewModel.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (viewModel.isSaving) "Saving..." else "Save")
                    }

                    viewModel.message?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                    viewModel.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
