package com.panomc.plugins.mediapage.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import com.panomc.platform.schema.EndpointDoc
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class GetMediaPagesAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao
) : Api() {
    override val paths = listOf(Path("/media-pages", RouteType.GET))

    override val doc = EndpointDoc(
        summary = "The active media pages.",
        tag = "media-pages",
        response = objectSchema()
            .requiredProperty(
                "items",
                arraySchema().items(
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
            )
    )

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository).build()

    override suspend fun handle(context: RoutingContext): Result {
        val sqlClient = databaseManager.getSqlClient()
        val pages = mediaPageDao.getAllActive(sqlClient)

        return Successful(mapOf("items" to pages))
    }
}
