package com.mtkach.campuspulse.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mtkach.campuspulse.data.CategoryEntity
import com.mtkach.campuspulse.data.ChronicleRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(repository: ChronicleRepository, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val categories by repository.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    var newName by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var editingName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Категорії") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)) {
            item {
                Row(modifier = Modifier.padding(vertical = 10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Нова категорія") },
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = {
                            val name = newName.trim()
                            if (name.isNotEmpty()) {
                                scope.launch { repository.addCategory(name) }
                                newName = ""
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    ) { Text("Додати") }
                }
            }
            items(categories, key = { it.id }) { category ->
                if (editingId == category.id) {
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {
                            val name = editingName.trim()
                            if (name.isNotEmpty()) {
                                scope.launch { repository.renameCategory(category, name) }
                            }
                            editingId = null
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "Зберегти назву")
                        }
                    }
                } else {
                    ListItem(
                        headlineContent = { Text(category.name) },
                        trailingContent = { CategoryActions(category, scope, repository) { editingId = category.id; editingName = category.name } },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryActions(
    category: CategoryEntity,
    scope: kotlinx.coroutines.CoroutineScope,
    repository: ChronicleRepository,
    onStartEdit: () -> Unit,
) {
    Row {
        IconButton(onClick = onStartEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Перейменувати категорію")
        }
        IconButton(onClick = { scope.launch { repository.deleteCategory(category) } }) {
            Icon(Icons.Filled.Delete, contentDescription = "Видалити категорію")
        }
    }
}
