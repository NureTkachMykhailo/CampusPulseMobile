package com.mtkach.campuspulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtkach.campuspulse.data.ChronicleRepository
import com.mtkach.campuspulse.data.Session
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    repository: ChronicleRepository,
    onLoggedIn: (Session) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("redaktor@campuspulse.local") }
    var password by remember { mutableStateOf("campus123") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вхід") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Демо-акаунти: redaktor@campuspulse.local / campus123 (Редакція, суперкористувач) " +
                    "або student@campuspulse.local / campus123 (Студрада, звичайний користувач).",
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") })

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = {
                scope.launch {
                    val session = repository.login(email, password)
                    if (session == null) {
                        error = "Невірний email або пароль"
                    } else {
                        onLoggedIn(session)
                    }
                }
            }) { Text("Увійти") }

            TextButton(onClick = {
                scope.launch {
                    val session = repository.register(email, password, email.substringBefore('@'))
                    onLoggedIn(session)
                }
            }) { Text("Зареєструватися з цим email") }
        }
    }
}
