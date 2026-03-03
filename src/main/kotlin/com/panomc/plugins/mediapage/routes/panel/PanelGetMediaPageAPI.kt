package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaCategoryDao
import com.panomc.plugins.mediapage.db.dao.MediaItemDao
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.numberSchema

@Endpoint
class PanelGetMediaPageAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao,
    private val mediaCategoryDao: MediaCategoryDao,
    private val mediaItemDao: MediaItemDao
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/media-pages/:id", RouteType.GET))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(param("id", numberSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val parameters = getParameters(context)
        val id = parameters.pathParameter("id").long
        val sqlClient = databaseManager.getSqlClient()

        val mediaPage = mediaPageDao.getById(id, sqlClient) ?: throw NotFound()

        val categories = mediaCategoryDao.getByPageId(id, sqlClient)
        val categoriesWithItems = categories.map { category ->
            val items = mediaItemDao.getByCategoryId(category.id, sqlClient)
            mapOf(
                "category" to category,
                "items" to items
            )
        }

        return Successful(
            mapOf(
                "mediaPage" to mediaPage,
                "categories" to categoriesWithItems
            )
        )
    }
}
