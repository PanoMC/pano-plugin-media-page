package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaCategoryDao
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import com.panomc.plugins.mediapage.db.model.MediaCategory
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class PanelAddCategoryAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao,
    private val mediaCategoryDao: MediaCategoryDao
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/media-pages/:pageId/categories", RouteType.POST))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun bodyHandler(): Handler<RoutingContext> = BodyHandler.create()

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(param("pageId", numberSchema()))
            .body(
                Bodies.json(
                    objectSchema()
                        .requiredProperty("title", stringSchema())
                        .optionalProperty("description", stringSchema())
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val parameters = getParameters(context)
        val pageId = parameters.pathParameter("pageId").long
        val data = parameters.body().jsonObject
        val sqlClient = databaseManager.getSqlClient()

        mediaPageDao.getById(pageId, sqlClient) ?: throw NotFound()

        val category = MediaCategory(
            pageId = pageId,
            title = data.getString("title"),
            description = data.getString("description") ?: ""
        )

        val id = mediaCategoryDao.add(category, sqlClient)

        return Successful(mapOf("id" to id))
    }
}
