package com.mtkach.campuspulse.data

import kotlinx.coroutines.flow.Flow

class ChronicleRepository(private val db: AppDatabase) {
    private val cache = FeedQueryCache()

    val categories: Flow<List<CategoryEntity>> = db.categoryDao().observeAll()

    suspend fun login(email: String, password: String): Session? {
        val user = db.userDao().findByCredentials(email.trim().lowercase(), password) ?: return null
        return Session(user.id, user.displayName, user.isSuperuser)
    }

    suspend fun register(email: String, password: String, displayName: String): Session {
        val id = db.userDao().insert(
            UserEntity(email = email.trim().lowercase(), password = password, displayName = displayName),
        )
        return Session(id, displayName, isSuperuser = false)
    }

    fun observeFeed(categoryId: Long?, query: String): Flow<List<ArticleWithMeta>> =
        db.articleDao().observeFeed(categoryId, query.trim())

    suspend fun loadFeedCached(categoryId: Long?, query: String): Pair<List<ArticleWithMeta>, Boolean> {
        cache.get(categoryId, query)?.let { return it to true }
        return emptyList<ArticleWithMeta>() to false
    }

    fun rememberFeedResult(categoryId: Long?, query: String, data: List<ArticleWithMeta>) {
        cache.put(categoryId, query, data)
    }

    fun observeArticle(id: Long): Flow<ArticleEntity?> = db.articleDao().observeById(id)

    fun observeComments(articleId: Long): Flow<List<CommentEntity>> = db.commentDao().observeForArticle(articleId)

    suspend fun saveArticle(article: ArticleEntity): Long {
        cache.invalidateAll()
        return if (article.id == 0L) {
            db.articleDao().insert(article)
        } else {
            db.articleDao().update(article)
            article.id
        }
    }

    suspend fun deleteArticle(id: Long) {
        cache.invalidateAll()
        db.articleDao().deleteById(id)
    }

    suspend fun addComment(comment: CommentEntity) {
        db.commentDao().insert(comment)
    }

    suspend fun deleteComment(comment: CommentEntity) {
        db.commentDao().delete(comment)
    }

    suspend fun addCategory(name: String) {
        cache.invalidateAll()
        db.categoryDao().insert(CategoryEntity(name = name))
    }

    suspend fun renameCategory(category: CategoryEntity, newName: String) {
        cache.invalidateAll()
        db.categoryDao().update(category.copy(name = newName))
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        cache.invalidateAll()
        db.categoryDao().delete(category)
    }

    fun canManageArticle(session: Session?, article: ArticleEntity): Boolean =
        session != null && (session.isSuperuser || session.displayName == article.authorName)

    fun canManageComment(session: Session?, comment: CommentEntity): Boolean =
        session != null && (session.isSuperuser || session.displayName == comment.authorName)

    suspend fun ensureSeeded() {
        if (db.userDao().count() > 0) return

        db.userDao().insert(
            UserEntity(email = "redaktor@campuspulse.local", password = "campus123", displayName = "Редакція", isSuperuser = true),
        )
        db.userDao().insert(
            UserEntity(email = "student@campuspulse.local", password = "campus123", displayName = "Студрада", isSuperuser = false),
        )

        val categoryNames = listOf("Навчання", "Гуртожиток", "Спорт", "Наука", "Події")
        val categoryIds = categoryNames.associateWith { db.categoryDao().insert(CategoryEntity(name = it)) }

        val now = System.currentTimeMillis()
        val seedArticles = listOf(
            Triple("Як пережити сесію на ПЗПІ без нервового зриву", "Навчання",
                "Сесія щороку лякає першокурсників, але виживають усі. Головне правило: не залишати " +
                    "конспекти на останню ніч і повторювати лабораторні заздалегідь."),
            Triple("Гуртожиток №5: що варто знати новачкам", "Гуртожиток",
                "Заселення, вахта, спільна кухня на поверсі — перші тижні завжди трохи хаотичні. " +
                    "Короткий гайд для тих, хто заселився вперше."),
            Triple("Збірна університету з баскетболу вийшла у фінал", "Спорт",
                "Студентська збірна ХНУРЕ здобула перемогу у півфіналі обласного чемпіонату. " +
                    "Фінальна гра — наступного тижня у спорткомплексі."),
            Triple("Науковий гурток запускає новий проєкт", "Наука",
                "Гурток машинного навчання оголосив набір учасників для проєкту з аналізу даних " +
                    "відкритих реєстрів. Досвід не обов'язковий."),
            Triple("День відкритих дверей: чого чекати абітурієнтам", "Події",
                "У суботу університет проведе день відкритих дверей: екскурсії лабораторіями, " +
                    "зустрічі з деканами та презентація студентського життя."),
        )

        val articleIds = seedArticles.mapIndexed { index, (title, category, body) ->
            db.articleDao().insert(
                ArticleEntity(
                    title = title,
                    body = body,
                    categoryId = categoryIds.getValue(category),
                    authorName = "Редакція",
                    createdAt = now - index * 3_600_000L,
                ),
            )
        }

        val seedComments = listOf(
            "Староста групи" to "Дуже доречно саме зараз, дякую!",
            "Куратор потоку" to "Збережу в закладки, надішлю першачкам.",
            "Голова студради" to "Класна ініціатива, підтримуємо.",
        )
        articleIds.take(2).forEachIndexed { articleIndex, articleId ->
            seedComments.forEachIndexed { commentIndex, (author, text) ->
                db.commentDao().insert(
                    CommentEntity(
                        articleId = articleId,
                        authorName = author,
                        body = text,
                        createdAt = now - (articleIndex * 10 + commentIndex) * 60_000L,
                    ),
                )
            }
        }
    }
}
