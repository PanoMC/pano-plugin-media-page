package com.panomc.plugins.mediapage.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaCategoryDao
import com.panomc.plugins.mediapage.db.dao.MediaItemDao
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.stringSchema

@Endpoint
class GetMediaPageBySlugAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao,
    private val mediaCategoryDao: MediaCategoryDao,
    private val mediaItemDao: MediaItemDao
) : Api() {
    override val paths = listOf(Path("/api/media-pages/:slug", RouteType.GET))

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler {
        return ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(param("slug", stringSchema()))
            .build()
    }

    override suspend fun handle(context: RoutingContext): Result {
        val slug = context.pathParam("slug")
        val sqlClient = databaseManager.getSqlClient()

        val mediaPage = mediaPageDao.getBySlug(slug, sqlClient)

        if (mediaPage == null || !mediaPage.active) {
            throw NotFound()
        }

        val allCategories = mediaCategoryDao.getByPageId(mediaPage.id, sqlClient)
        val categories = allCategories.filter { it.visible }
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
