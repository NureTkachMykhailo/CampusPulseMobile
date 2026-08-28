package com.mtkach.campuspulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mtkach.campuspulse.data.ChronicleRepository
import com.mtkach.campuspulse.data.CommentEntity
import com.mtkach.campuspulse.data.Session
import com.mtkach.campuspulse.ui.components.AccountAvatar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    repository: ChronicleRepository,
    session: Session?,
    articleId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    onRequireLogin: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val article by repository.observeArticle(articleId).collectAsStateWithLifecycle(initialValue = null)
    val comments by repository.observeComments(articleId).collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by repository.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    var draft by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Стаття") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        val current = article
        if (current == null) {
            Text("Завантаження...", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }

        val categoryName = categories.find { it.id == current.categoryId }?.name ?: ""
        val canManage = repository.canManageArticle(session, current)
        val dateText = remember(current.createdAt) {
            SimpleDateFormat("dd.MM.yyyy", Locale("uk")).format(Date(current.createdAt))
        }

        LazyColumn(modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)) {
            item {
                Text(categoryName, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Text(current.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${current.authorName} · $dateText", style = MaterialTheme.typography.labelSmall)
                Text(current.body, modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))

                if (canManage) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onEdit(current.id) }) { Text("Редагувати") }
                        Button(onClick = {
                            scope.launch {
                                repository.deleteArticle(current.id)
                                onDeleted()
                            }
                        }) { Text("Видалити") }
                    }
                } else if (session == null) {
                    Text("Увійдіть, щоб редагувати або видаляти статті.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Редагувати може лише автор або суперкористувач.", style = MaterialTheme.typography.bodySmall)
                }

                Text("Коментарі (${comments.size})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }

            items(comments, key = { it.id }) { comment ->
                CommentRow(
                    comment = comment,
                    canDelete = repository.canManageComment(session, comment),
                    onDelete = { scope.launch { repository.deleteComment(comment) } },
                )
            }

            item {
                if (session != null) {
                    Column(modifier = Modifier.padding(top = 10.dp, bottom = 24.dp)) {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            label = { Text("Ваш коментар") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            enabled = draft.isNotBlank(),
                            onClick = {
                                scope.launch {
                                    repository.addComment(
                                        CommentEntity(
                                            articleId = current.id,
                                            authorName = session.displayName,
                                            body = draft.trim(),
                                            createdAt = System.currentTimeMillis(),
                                        ),
                                    )
                                    draft = ""
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Надіслати") }
                    }
                } else {
                    OutlinedButton(onClick = onRequireLogin, modifier = Modifier.padding(vertical = 16.dp)) {
                        Text("Увійдіть, щоб залишити коментар")
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: CommentEntity, canDelete: Boolean, onDelete: () -> Unit) {
    val timeText = remember(comment.createdAt) {
        SimpleDateFormat("dd.MM HH:mm", Locale("uk")).format(Date(comment.createdAt))
    }
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(12.dp)) {
            AccountAvatar(name = comment.authorName)
            Column(modifier = Modifier
                .padding(start = 10.dp)
                .fillMaxWidth()) {
                Text("${comment.authorName} · $timeText", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text(comment.body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
                if (canDelete) {
                    androidx.compose.material3.TextButton(
                        onClick = onDelete,
                        modifier = Modifier.padding(top = 2.dp),
                    ) { Text("Видалити", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}
