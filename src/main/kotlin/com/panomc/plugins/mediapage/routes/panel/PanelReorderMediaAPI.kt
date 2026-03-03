package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaItemDao
import com.panomc.plugins.mediapage.db.model.MediaItem
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.arraySchema
import io.vertx.json.schema.common.dsl.Schemas.numberSchema
import io.vertx.json.schema.common.dsl.Schemas.objectSchema

@Endpoint
class PanelReorderMediaAPI(
    private val plugin: MediaPagePlugin,
    private val mediaItemDao: MediaItemDao
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/media-items/reorder", RouteType.PUT))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .body(
                Bodies.json(
                    objectSchema()
                        .requiredProperty("items", arraySchema().items(
                            objectSchema()
                                .requiredProperty("id", numberSchema())
                                .requiredProperty("sortOrder", numberSchema())
                        ))
                )
            )
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject
        val items = data.getJsonArray("items")
        val sqlClient = databaseManager.getSqlClient()

        val mediaItems = items.map { item ->
            val obj = item as io.vertx.core.json.JsonObject
            MediaItem(
                id = obj.getLong("id"),
                sortOrder = obj.getInteger("sortOrder")
            )
        }

        mediaItemDao.updateSortOrders(mediaItems, sqlClient)

        return Successful()
    }
}
