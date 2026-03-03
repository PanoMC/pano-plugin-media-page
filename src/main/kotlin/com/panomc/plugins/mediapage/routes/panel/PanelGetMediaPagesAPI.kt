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
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.numberSchema
import kotlin.math.ceil

@Endpoint
class PanelGetMediaPagesAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/media-pages", RouteType.GET))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler {
        return ValidationHandlerBuilder.create(schemaRepository)
            .queryParameter(optionalParam("page", numberSchema()))
            .build()
    }

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val parameters = getParameters(context)
        val page = parameters.queryParameter("page")?.long ?: 1L

        val sqlClient = databaseManager.getSqlClient()
        val pages = mediaPageDao.getAllByPage(page, sqlClient)
        val count = mediaPageDao.count(sqlClient)

        var totalPageNum = ceil(count.toDouble() / 10).toLong()
        if (totalPageNum == 0L) totalPageNum = 1L

        return Successful(
            mapOf(
                "mediaPages" to pages,
                "totalPage" to totalPageNum,
                "pageCount" to count
            )
        )
    }
}
