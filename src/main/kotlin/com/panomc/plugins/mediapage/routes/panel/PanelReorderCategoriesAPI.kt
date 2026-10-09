package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaCategoryDao
import com.panomc.plugins.mediapage.db.model.MediaCategory
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Bodies
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.arraySchema
import io.vertx.json.schema.common.dsl.Schemas.numberSchema
import io.vertx.json.schema.common.dsl.Schemas.objectSchema

@Endpoint
class PanelReorderCategoriesAPI(
    private val plugin: MediaPagePlugin,
    private val mediaCategoryDao: MediaCategoryDao
) : PanelApi() {
    override val paths = listOf(Path("/media-categories/reorder", RouteType.PUT))

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
                        .requiredProperty("categories", arraySchema().items(
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
        val items = data.getJsonArray("categories")
        val sqlClient = databaseManager.getSqlClient()

        val categories = items.map { item ->
            val obj = item as io.vertx.core.json.JsonObject
            MediaCategory(
                id = obj.getLong("id"),
                sortOrder = obj.getInteger("sortOrder")
            )
        }

        mediaCategoryDao.updateSortOrders(categories, sqlClient)

        return Successful()
    }
}
