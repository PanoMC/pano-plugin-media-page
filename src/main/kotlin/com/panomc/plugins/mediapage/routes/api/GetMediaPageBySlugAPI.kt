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
import com.panomc.platform.schema.dsl.Parameters.param
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.stringSchema
import com.panomc.platform.schema.EndpointDoc
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class GetMediaPageBySlugAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao,
    private val mediaCategoryDao: MediaCategoryDao,
    private val mediaItemDao: MediaItemDao
) : Api() {
    override val paths = listOf(Path("/media-pages/:slug", RouteType.GET))

    override val doc = EndpointDoc(
        summary = "One media page with its visible categories and their items.",
        tag = "media-pages",
        response = objectSchema()
            .requiredProperty(
                "mediaPage",
                objectSchema()
                .requiredProperty("id", intSchema())
                .requiredProperty("title", stringSchema())
                .requiredProperty("slug", stringSchema())
                .requiredProperty("active", booleanSchema())
                .optionalProperty("description", stringSchema())
                .optionalProperty("registerToThemeNav", booleanSchema())
                .optionalProperty("sortOrder", intSchema())
                .optionalProperty("createdAt", intSchema())
                .optionalProperty("updatedAt", intSchema())
            )
            .requiredProperty(
                "categories",
                arraySchema().items(
                    objectSchema()
                        .requiredProperty(
                            "category",
                            objectSchema()
                            .requiredProperty("id", intSchema())
                            .requiredProperty("pageId", intSchema())
                            .requiredProperty("title", stringSchema())
                            .optionalProperty("description", stringSchema())
                            .optionalProperty("visible", booleanSchema())
                            .optionalProperty("sortOrder", intSchema())
                            .optionalProperty("createdAt", intSchema())
                        )
                        .requiredProperty(
                            "items",
                            arraySchema().items(
                                objectSchema()
                            .requiredProperty("id", intSchema())
                            .requiredProperty("categoryId", intSchema())
                            .requiredProperty("type", stringSchema())
                            .requiredProperty("filePath", stringSchema())
                            .optionalProperty("title", stringSchema())
                            .optionalProperty("description", stringSchema())
                            .optionalProperty("thumbnailPath", stringSchema())
                            .optionalProperty("originalFileName", stringSchema())
                            .optionalProperty("mimeType", stringSchema())
                            .optionalProperty("fileSize", intSchema())
                            .optionalProperty("width", intSchema())
                            .optionalProperty("height", intSchema())
                            .optionalProperty("duration", intSchema())
                            .optionalProperty("sortOrder", intSchema())
                            .optionalProperty("createdAt", intSchema())
                            )
                        )
                )
            ),
        errors = listOf(NotFound::class)
    )

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
