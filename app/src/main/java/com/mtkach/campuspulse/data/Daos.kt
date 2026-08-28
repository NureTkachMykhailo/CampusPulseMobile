package com.mtkach.campuspulse.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun findByCredentials(email: String, password: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}

@Dao
interface ArticleDao {
    @Query(
        """
        SELECT a.id, a.title, a.body, a.categoryId, c.name AS categoryName,
               a.authorName, a.createdAt,
               (SELECT COUNT(*) FROM comments cm WHERE cm.articleId = a.id) AS commentCount
        FROM articles a
        JOIN categories c ON c.id = a.categoryId
        WHERE (:categoryId IS NULL OR a.categoryId = :categoryId)
          AND (
            :query = '' OR
            a.title LIKE '%' || :query || '%' COLLATE NOCASE OR
            a.body LIKE '%' || :query || '%' COLLATE NOCASE OR
            a.authorName LIKE '%' || :query || '%' COLLATE NOCASE
          )
        ORDER BY a.createdAt DESC
        """,
    )
    fun observeFeed(categoryId: Long?, query: String): Flow<List<ArticleWithMeta>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun observeById(id: Long): Flow<ArticleEntity?>

    @Insert
    suspend fun insert(article: ArticleEntity): Long

    @Update
    suspend fun update(article: ArticleEntity)

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun count(): Int
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE articleId = :articleId ORDER BY createdAt ASC")
    fun observeForArticle(articleId: Long): Flow<List<CommentEntity>>

    @Insert
    suspend fun insert(comment: CommentEntity): Long

    @Delete
    suspend fun delete(comment: CommentEntity)
}
