package com.panomc.plugins.mediapage.db.impl

import com.panomc.platform.annotation.Dao
import com.panomc.plugins.mediapage.db.dao.MediaCategoryDao
import com.panomc.plugins.mediapage.db.model.MediaCategory
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.mysqlclient.MySQLClient
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope

@Dao
@Lazy
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class MediaCategoryDaoImpl : MediaCategoryDao() {

    override suspend fun init(sqlClient: SqlClient) {
        sqlClient
            .query(
                """
                    CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                      `id` bigint NOT NULL AUTO_INCREMENT,
                      `pageId` bigint NOT NULL,
                      `title` VARCHAR(500) NOT NULL,
                      `description` TEXT,
                      `visible` TINYINT(1) NOT NULL DEFAULT 1,
                      `sortOrder` INT NOT NULL DEFAULT 0,
                      `createdAt` BIGINT(20) NOT NULL,
                      PRIMARY KEY (`id`),
                      KEY `pageId` (`pageId`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Media categories within gallery pages.';
                """
            )
            .execute()
            .coAwait()

        // Add visible column if it doesn't exist (migration)
        try {
            sqlClient
                .query("ALTER TABLE `${getTablePrefix() + tableName}` ADD COLUMN `visible` TINYINT(1) NOT NULL DEFAULT 1 AFTER `description`")
                .execute()
                .coAwait()
        } catch (_: Exception) {
            // Column already exists
        }
    }

    override suspend fun add(category: MediaCategory, sqlClient: SqlClient): Long {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`pageId`, `title`, `description`, `visible`, `sortOrder`, `createdAt`) VALUES (?, ?, ?, ?, ?, ?)"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    category.pageId,
                    category.title,
                    category.description,
                    if (category.visible) 1 else 0,
                    category.sortOrder,
                    category.createdAt
                )
            )
            .coAwait()

        return rows.property(MySQLClient.LAST_INSERTED_ID)
    }

    override suspend fun update(category: MediaCategory, sqlClient: SqlClient) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET `title` = ?, `description` = ?, `visible` = ?, `sortOrder` = ? WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    category.title,
                    category.description,
                    if (category.visible) 1 else 0,
                    category.sortOrder,
                    category.id
                )
            )
            .coAwait()
    }

    override suspend fun getByPageId(pageId: Long, sqlClient: SqlClient): List<MediaCategory> {
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `pageId` = ? ORDER BY `sortOrder` ASC, `id` ASC"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(pageId))
            .coAwait()

        return rows.toEntities()
    }

    override suspend fun deleteById(id: Long, sqlClient: SqlClient) {
        val query = "DELETE FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .coAwait()
    }

    override suspend fun deleteByPageId(pageId: Long, sqlClient: SqlClient) {
        val query = "DELETE FROM `${getTablePrefix() + tableName}` WHERE `pageId` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(pageId))
            .coAwait()
    }

    override suspend fun getById(id: Long, sqlClient: SqlClient): MediaCategory? {
        val query = "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .coAwait()

        return rows.toEntities().getOrNull(0)
    }

    override suspend fun count(sqlClient: SqlClient): Long {
        val query = "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}`"

        val rows: RowSet<Row> = sqlClient
            .query(query)
            .execute()
            .coAwait()

        if (rows.size() == 0) return 0L
        return rows.toList()[0].getLong(0)
    }

    override suspend fun updateSortOrders(categories: List<MediaCategory>, sqlClient: SqlClient) {
        categories.forEach { category ->
            val query = "UPDATE `${getTablePrefix() + tableName}` SET `sortOrder` = ? WHERE `id` = ?"
            sqlClient
                .preparedQuery(query)
                .execute(Tuple.of(category.sortOrder, category.id))
                .coAwait()
        }
    }

    override suspend fun updateVisibility(id: Long, visible: Boolean, sqlClient: SqlClient) {
        val query = "UPDATE `${getTablePrefix() + tableName}` SET `visible` = ? WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(if (visible) 1 else 0, id))
            .coAwait()
    }

    override suspend fun moveToPage(id: Long, newPageId: Long, sqlClient: SqlClient) {
        val query = "UPDATE `${getTablePrefix() + tableName}` SET `pageId` = ?, `sortOrder` = 0 WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(newPageId, id))
            .coAwait()
    }

    override suspend fun uninstall(sqlClient: SqlClient) {
        sqlClient
            .query("DROP TABLE IF EXISTS `${getTablePrefix() + tableName}`")
            .execute()
            .coAwait()
    }
}
