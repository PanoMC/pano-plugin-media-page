package com.panomc.plugins.mediapage.db.dao

import com.panomc.platform.db.Dao
import com.panomc.plugins.mediapage.db.model.MediaPage
import io.vertx.sqlclient.SqlClient

abstract class MediaPageDao : Dao<MediaPage>(MediaPage::class.java) {
    abstract suspend fun add(mediaPage: MediaPage, sqlClient: SqlClient): Long

    abstract suspend fun update(mediaPage: MediaPage, sqlClient: SqlClient)

    abstract suspend fun getAllByPage(page: Long, sqlClient: SqlClient): List<MediaPage>

    abstract suspend fun count(sqlClient: SqlClient): Long

    abstract suspend fun deleteById(id: Long, sqlClient: SqlClient)

    abstract suspend fun getById(id: Long, sqlClient: SqlClient): MediaPage?

    abstract suspend fun getBySlug(slug: String, sqlClient: SqlClient): MediaPage?

    abstract suspend fun getAllActive(sqlClient: SqlClient): List<MediaPage>

    abstract suspend fun getAll(sqlClient: SqlClient): List<MediaPage>
}
