package com.mtkach.campuspulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mtkach.campuspulse.data.ArticleWithMeta
import com.mtkach.campuspulse.data.ChronicleRepository
import com.mtkach.campuspulse.data.Session
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    repository: ChronicleRepository,
    session: Session?,
    onOpenArticle: (Long) -> Unit,
    onNewArticle: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenLogin: () -> Unit,
    onLogout: () -> Unit,
) {
    val categories by repository.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var query by remember { mutableStateOf("") }
    var fromCache by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    val articles by repository.observeFeed(selectedCategoryId, query).collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(selectedCategoryId, query, articles) {
        val (_, cached) = repository.loadFeedCached(selectedCategoryId, query)
        fromCache = cached
        repository.rememberFeedResult(selectedCategoryId, query, articles)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CampusPulse", style = MaterialTheme.typography.headlineSmall)
                        Text("студентське життя ХНУРЕ", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenCategories) {
                        Icon(Icons.Filled.List, contentDescription = "Категорії")
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Акаунт")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (session == null) {
                            DropdownMenuItem(text = { Text("Увійти") }, onClick = { menuOpen = false; onOpenLogin() })
                        } else {
                            DropdownMenuItem(text = { Text("Вийти (${session.displayName})") }, onClick = { menuOpen = false; onLogout() })
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)) {

            Row(modifier = Modifier.padding(top = 8.dp)) {
                androidx.compose.material3.Button(onClick = onNewArticle) { Text("Нова стаття") }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Пошук: заголовок, текст або автор") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 10.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("Усі") },
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.name) },
                    )
                }
            }

            if (fromCache) {
                Text(
                    "Результат із кешу запиту",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(articles, key = { it.id }) { article ->
                    ArticleCard(article, onClick = { onOpenArticle(article.id) })
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: ArticleWithMeta, onClick: () -> Unit) {
    val dateText = remember(article.createdAt) {
        SimpleDateFormat("dd.MM.yyyy", Locale("uk")).format(Date(article.createdAt))
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(article.categoryName, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(article.body.take(110) + "...", style = MaterialTheme.typography.bodyMedium)
            Text(
                "${article.authorName} · $dateText · ${article.commentCount} коментарів",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
