package com.panomc.plugins.mediapage.db.impl

import com.panomc.platform.annotation.Dao
import com.panomc.plugins.mediapage.db.dao.MediaItemDao
import com.panomc.plugins.mediapage.db.model.MediaItem
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
class MediaItemDaoImpl : MediaItemDao() {

    override suspend fun init(sqlClient: SqlClient) {
        sqlClient
            .query(
                """
                    CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                      `id` bigint NOT NULL AUTO_INCREMENT,
                      `categoryId` bigint NOT NULL,
                      `type` VARCHAR(20) NOT NULL DEFAULT 'IMAGE',
                      `title` VARCHAR(500) NOT NULL DEFAULT '',
                      `description` TEXT,
                      `filePath` VARCHAR(1000) NOT NULL,
                      `thumbnailPath` VARCHAR(1000) NOT NULL DEFAULT '',
                      `originalFileName` VARCHAR(500) NOT NULL DEFAULT '',
                      `mimeType` VARCHAR(100) NOT NULL DEFAULT '',
                      `fileSize` BIGINT(20) NOT NULL DEFAULT 0,
                      `width` INT NOT NULL DEFAULT 0,
                      `height` INT NOT NULL DEFAULT 0,
                      `duration` INT NOT NULL DEFAULT 0,
                      `sortOrder` INT NOT NULL DEFAULT 0,
                      `createdAt` BIGINT(20) NOT NULL,
                      PRIMARY KEY (`id`),
                      KEY `categoryId` (`categoryId`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Media items (images, videos) in categories.';
                """
            )
            .execute()
            .coAwait()
    }

    override suspend fun add(item: MediaItem, sqlClient: SqlClient): Long {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`categoryId`, `type`, `title`, `description`, `filePath`, `thumbnailPath`, `originalFileName`, `mimeType`, `fileSize`, `width`, `height`, `duration`, `sortOrder`, `createdAt`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    item.categoryId,
                    item.type,
                    item.title,
                    item.description,
                    item.filePath,
                    item.thumbnailPath,
                    item.originalFileName,
                    item.mimeType,
                    item.fileSize,
                    item.width,
                    item.height,
                    item.duration,
                    item.sortOrder,
                    item.createdAt
                )
            )
            .coAwait()

        return rows.property(MySQLClient.LAST_INSERTED_ID)
    }

    override suspend fun update(item: MediaItem, sqlClient: SqlClient) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET `title` = ?, `description` = ?, `sortOrder` = ? WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    item.title,
                    item.description,
                    item.sortOrder,
                    item.id
                )
            )
            .coAwait()
    }

    override suspend fun getByCategoryId(categoryId: Long, sqlClient: SqlClient): List<MediaItem> {
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `categoryId` = ? ORDER BY `sortOrder` ASC, `id` ASC"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(categoryId))
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

    override suspend fun deleteByCategoryId(categoryId: Long, sqlClient: SqlClient) {
        val query = "DELETE FROM `${getTablePrefix() + tableName}` WHERE `categoryId` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(categoryId))
            .coAwait()
    }

    override suspend fun getById(id: Long, sqlClient: SqlClient): MediaItem? {
        val query = "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .coAwait()

        return rows.toEntities().getOrNull(0)
    }

    override suspend fun countByCategoryId(categoryId: Long, sqlClient: SqlClient): Long {
        val query = "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` WHERE `categoryId` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(categoryId))
            .coAwait()

        if (rows.size() == 0) return 0L
        return rows.toList()[0].getLong(0)
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

    override suspend fun getByPageId(pageId: Long, sqlClient: SqlClient): List<MediaItem> {
        val categoryTable = getTablePrefix() + "media_category"
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `categoryId` IN (SELECT `id` FROM `$categoryTable` WHERE `pageId` = ?) ORDER BY `sortOrder` ASC, `id` ASC"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(pageId))
            .coAwait()

        return rows.toEntities()
    }

    override suspend fun updateSortOrders(items: List<MediaItem>, sqlClient: SqlClient) {
        items.forEach { item ->
            val query = "UPDATE `${getTablePrefix() + tableName}` SET `sortOrder` = ? WHERE `id` = ?"
            sqlClient
                .preparedQuery(query)
                .execute(Tuple.of(item.sortOrder, item.id))
                .coAwait()
        }
    }

    override suspend fun uninstall(sqlClient: SqlClient) {
        sqlClient
            .query("DROP TABLE IF EXISTS `${getTablePrefix() + tableName}`")
            .execute()
            .coAwait()
    }
}
