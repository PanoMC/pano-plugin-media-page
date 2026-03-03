package com.panomc.plugins.mediapage.db.impl

import com.panomc.platform.annotation.Dao
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import com.panomc.plugins.mediapage.db.model.MediaPage
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
class MediaPageDaoImpl : MediaPageDao() {

    override suspend fun init(sqlClient: SqlClient) {
        sqlClient
            .query(
                """
                    CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                      `id` bigint NOT NULL AUTO_INCREMENT,
                      `title` VARCHAR(500) NOT NULL,
                      `slug` VARCHAR(255) NOT NULL,
                      `description` TEXT,
                      `active` TINYINT(1) NOT NULL DEFAULT 1,
                      `registerToThemeNav` TINYINT(1) NOT NULL DEFAULT 1,
                      `sortOrder` INT NOT NULL DEFAULT 0,
                      `createdAt` BIGINT(20) NOT NULL,
                      `updatedAt` BIGINT(20) NOT NULL,
                      PRIMARY KEY (`id`),
                      UNIQUE KEY `slug` (`slug`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Media gallery pages.';
                """
            )
            .execute()
            .coAwait()
    }

    override suspend fun add(mediaPage: MediaPage, sqlClient: SqlClient): Long {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`title`, `slug`, `description`, `active`, `registerToThemeNav`, `sortOrder`, `createdAt`, `updatedAt`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    mediaPage.title,
                    mediaPage.slug,
                    mediaPage.description,
                    mediaPage.active,
                    mediaPage.registerToThemeNav,
                    mediaPage.sortOrder,
                    mediaPage.createdAt,
                    mediaPage.updatedAt
                )
            )
            .coAwait()

        return rows.property(MySQLClient.LAST_INSERTED_ID)
    }

    override suspend fun update(mediaPage: MediaPage, sqlClient: SqlClient) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET `title` = ?, `slug` = ?, `description` = ?, `active` = ?, `registerToThemeNav` = ?, `sortOrder` = ?, `updatedAt` = ? WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    mediaPage.title,
                    mediaPage.slug,
                    mediaPage.description,
                    mediaPage.active,
                    mediaPage.registerToThemeNav,
                    mediaPage.sortOrder,
                    mediaPage.updatedAt,
                    mediaPage.id
                )
            )
            .coAwait()
    }

    override suspend fun getAllByPage(page: Long, sqlClient: SqlClient): List<MediaPage> {
        val offset = (page - 1) * 10
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` ORDER BY `sortOrder` ASC, `id` DESC LIMIT 10 OFFSET ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(offset))
            .coAwait()

        return rows.toEntities()
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

    override suspend fun deleteById(id: Long, sqlClient: SqlClient) {
        val query = "DELETE FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .coAwait()
    }

    override suspend fun getById(id: Long, sqlClient: SqlClient): MediaPage? {
        val query = "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .coAwait()

        return rows.toEntities().getOrNull(0)
    }

    override suspend fun getBySlug(slug: String, sqlClient: SqlClient): MediaPage? {
        val query = "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `slug` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(slug))
            .coAwait()

        return rows.toEntities().getOrNull(0)
    }

    override suspend fun getAllActive(sqlClient: SqlClient): List<MediaPage> {
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `active` = ? ORDER BY `sortOrder` ASC, `id` DESC"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(true))
            .coAwait()

        return rows.toEntities()
    }

    override suspend fun getAll(sqlClient: SqlClient): List<MediaPage> {
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` ORDER BY `sortOrder` ASC, `id` DESC"

        val rows: RowSet<Row> = sqlClient
            .query(query)
            .execute()
            .coAwait()

        return rows.toEntities()
    }

    override suspend fun uninstall(sqlClient: SqlClient) {
        sqlClient
            .query("DROP TABLE IF EXISTS `${getTablePrefix() + tableName}`")
            .execute()
            .coAwait()
    }
}
