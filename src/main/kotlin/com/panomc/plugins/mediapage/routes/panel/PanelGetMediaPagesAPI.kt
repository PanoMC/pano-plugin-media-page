package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository

@Endpoint
class PanelGetMediaPagesAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao
) : PanelApi() {
    override val paths = listOf(Path("/media-pages", RouteType.GET))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler {
        return Paging.params(ValidationHandlerBuilder.create(schemaRepository)).build()
    }

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val page = Paging.request(context)

        val sqlClient = databaseManager.getSqlClient()
        val count = mediaPageDao.count(sqlClient)

        Paging.requireInRange(page, count)

        val pages = mediaPageDao.getAllByPage(page, sqlClient)

        return Successful(Paging.response(pages, count, page))
    }
}
