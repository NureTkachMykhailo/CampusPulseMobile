package com.mtkach.campuspulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mtkach.campuspulse.data.ArticleEntity
import com.mtkach.campuspulse.data.ChronicleRepository
import com.mtkach.campuspulse.data.Session
import kotlinx.coroutines.launch

private val stepTitles = listOf("Мета", "Категорія", "Текст", "Огляд")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleFormScreen(
    repository: ChronicleRepository,
    session: Session,
    articleId: Long?,
    onDone: (Long) -> Unit,
    onCancel: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val categories by repository.categories.collectAsStateWithLifecycle(initialValue = emptyList())

    var step by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var body by remember { mutableStateOf("") }
    var categoryMenuOpen by remember { mutableStateOf(false) }

    val articleFlow = remember(articleId) {
        if (articleId != null) repository.observeArticle(articleId) else kotlinx.coroutines.flow.flowOf(null)
    }
    val existing by articleFlow.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(existing) {
        existing?.let {
            title = it.title
            body = it.body
            categoryId = it.categoryId
        }
    }
    LaunchedEffect(categories) {
        if (categoryId == null && categories.isNotEmpty()) categoryId = categories.first().id
    }

    val stepValid = when (step) {
        0 -> title.isNotBlank()
        1 -> categoryId != null
        2 -> body.isNotBlank()
        else -> true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (articleId == null) "Нова стаття" else "Редагування статті") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Скасувати")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp)) {

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                stepTitles.forEachIndexed { index, _ ->
                    StepDot(active = index == step, done = index < step)
                }
            }
            Text(
                "Крок ${step + 1} з ${stepTitles.size}: ${stepTitles[step]}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )

            when (step) {
                0 -> OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок статті") },
                    modifier = Modifier.fillMaxWidth(),
                )

                1 -> Box {
                    OutlinedTextField(
                        value = categories.find { it.id == categoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категорія") },
                        trailingIcon = {
                            IconButton(onClick = { categoryMenuOpen = true }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Обрати категорію")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DropdownMenu(expanded = categoryMenuOpen, onDismissRequest = { categoryMenuOpen = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { categoryId = category.id; categoryMenuOpen = false },
                            )
                        }
                    }
                }

                2 -> OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Текст статті") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                )

                3 -> Card {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(categories.find { it.id == categoryId }?.name ?: "", color = MaterialTheme.colorScheme.primary)
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(body, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (step > 0) {
                    OutlinedButton(onClick = { step -= 1 }) { Text("Назад") }
                }
                if (step < stepTitles.lastIndex) {
                    Button(enabled = stepValid, onClick = { step += 1 }) { Text("Далі") }
                } else {
                    Button(onClick = {
                        scope.launch {
                            val savedId = repository.saveArticle(
                                ArticleEntity(
                                    id = articleId ?: 0L,
                                    title = title.trim(),
                                    body = body.trim(),
                                    categoryId = categoryId ?: categories.first().id,
                                    authorName = existing?.authorName ?: session.displayName,
                                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                                ),
                            )
                            onDone(if (articleId != null) articleId else savedId)
                        }
                    }) { Text(if (articleId == null) "Опублікувати" else "Зберегти") }
                }
            }
        }
    }
}

@Composable
private fun StepDot(active: Boolean, done: Boolean) {
    val color = when {
        active -> MaterialTheme.colorScheme.primary
        done -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {}
}
