package com.panomc.plugins.mediapage.db.dao

import com.panomc.platform.db.Dao
import com.panomc.plugins.mediapage.db.model.MediaItem
import io.vertx.sqlclient.SqlClient

abstract class MediaItemDao : Dao<MediaItem>(MediaItem::class.java) {
    abstract suspend fun add(item: MediaItem, sqlClient: SqlClient): Long

    abstract suspend fun update(item: MediaItem, sqlClient: SqlClient)

    abstract suspend fun getByCategoryId(categoryId: Long, sqlClient: SqlClient): List<MediaItem>

    abstract suspend fun deleteById(id: Long, sqlClient: SqlClient)

    abstract suspend fun deleteByCategoryId(categoryId: Long, sqlClient: SqlClient)

    abstract suspend fun getById(id: Long, sqlClient: SqlClient): MediaItem?

    abstract suspend fun countByCategoryId(categoryId: Long, sqlClient: SqlClient): Long

    abstract suspend fun count(sqlClient: SqlClient): Long

    abstract suspend fun getByPageId(pageId: Long, sqlClient: SqlClient): List<MediaItem>

    abstract suspend fun updateSortOrders(items: List<MediaItem>, sqlClient: SqlClient)
}
