package com.mtkach.campuspulse.data

/**
 * Мобільна адаптація ідеї кеш-шару без HTTP-сервера: тримає останню
 * відповідь loadFeed(categoryId, query) у пам'яті з коротким TTL.
 */
class FeedQueryCache(private val ttlMillis: Long = 30_000) {
    private data class Entry(val data: List<ArticleWithMeta>, val storedAt: Long)

    private val entries = mutableMapOf<String, Entry>()

    private fun key(categoryId: Long?, query: String) = "${categoryId ?: "all"}::${query.lowercase()}"

    fun get(categoryId: Long?, query: String): List<ArticleWithMeta>? {
        val entry = entries[key(categoryId, query)] ?: return null
        val fresh = System.currentTimeMillis() - entry.storedAt < ttlMillis
        return if (fresh) entry.data else null
    }

    fun put(categoryId: Long?, query: String, data: List<ArticleWithMeta>) {
        entries[key(categoryId, query)] = Entry(data, System.currentTimeMillis())
    }

    fun invalidateAll() {
        entries.clear()
    }
}
