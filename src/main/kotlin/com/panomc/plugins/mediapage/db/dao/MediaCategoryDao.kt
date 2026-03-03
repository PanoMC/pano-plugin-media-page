package com.panomc.plugins.mediapage.db.dao

import com.panomc.platform.db.Dao
import com.panomc.plugins.mediapage.db.model.MediaCategory
import io.vertx.sqlclient.SqlClient

abstract class MediaCategoryDao : Dao<MediaCategory>(MediaCategory::class.java) {
    abstract suspend fun add(category: MediaCategory, sqlClient: SqlClient): Long

    abstract suspend fun update(category: MediaCategory, sqlClient: SqlClient)

    abstract suspend fun getByPageId(pageId: Long, sqlClient: SqlClient): List<MediaCategory>

    abstract suspend fun deleteById(id: Long, sqlClient: SqlClient)

    abstract suspend fun deleteByPageId(pageId: Long, sqlClient: SqlClient)

    abstract suspend fun getById(id: Long, sqlClient: SqlClient): MediaCategory?

    abstract suspend fun count(sqlClient: SqlClient): Long

    abstract suspend fun updateSortOrders(categories: List<MediaCategory>, sqlClient: SqlClient)

    abstract suspend fun updateVisibility(id: Long, visible: Boolean, sqlClient: SqlClient)

    abstract suspend fun moveToPage(id: Long, newPageId: Long, sqlClient: SqlClient)
}
