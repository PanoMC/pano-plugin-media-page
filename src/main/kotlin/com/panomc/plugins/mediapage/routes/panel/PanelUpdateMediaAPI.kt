package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaItemDao
import com.panomc.plugins.mediapage.db.model.MediaItem
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Bodies.json
import com.panomc.platform.schema.dsl.Parameters.param
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class PanelUpdateMediaAPI(
    private val plugin: MediaPagePlugin,
    private val mediaItemDao: MediaItemDao
) : PanelApi() {
    override val paths = listOf(Path("/media-items/:id", RouteType.PUT))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun bodyHandler(): Handler<RoutingContext> = BodyHandler.create()

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(param("id", numberSchema()))
            .body(
                json(
                    objectSchema()
                        .optionalProperty("title", stringSchema())
                        .optionalProperty("description", stringSchema())
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val parameters = getParameters(context)
        val id = parameters.pathParameter("id").long
        val data = parameters.body().jsonObject
        val sqlClient = databaseManager.getSqlClient()

        val existing = mediaItemDao.getById(id, sqlClient) ?: throw NotFound()

        val updated = MediaItem(
            id = id,
            categoryId = existing.categoryId,
            type = existing.type,
            title = data.getString("title") ?: existing.title,
            description = data.getString("description") ?: existing.description,
            filePath = existing.filePath,
            thumbnailPath = existing.thumbnailPath,
            originalFileName = existing.originalFileName,
            mimeType = existing.mimeType,
            fileSize = existing.fileSize,
            width = existing.width,
            height = existing.height,
            duration = existing.duration,
            sortOrder = existing.sortOrder,
            createdAt = existing.createdAt
        )

        mediaItemDao.update(updated, sqlClient)

        return Successful()
    }
}
