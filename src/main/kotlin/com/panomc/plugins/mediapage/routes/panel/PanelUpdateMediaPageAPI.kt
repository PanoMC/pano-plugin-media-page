package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.BadRequest
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import com.panomc.plugins.mediapage.db.model.MediaPage
import com.panomc.plugins.mediapage.error.SlugAlreadyExists
import com.panomc.plugins.mediapage.log.UpdatedMediaPageLog
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
class PanelUpdateMediaPageAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao
) : PanelApi() {
    override val paths = listOf(Path("/media-pages/:id", RouteType.PUT))

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
                        .requiredProperty("title", stringSchema())
                        .requiredProperty("slug", stringSchema())
                        .optionalProperty("description", stringSchema())
                        .requiredProperty("active", booleanSchema())
                        .requiredProperty("registerToThemeNav", booleanSchema())
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

        val existingPage = mediaPageDao.getById(id, sqlClient) ?: throw NotFound()

        val slug = data.getString("slug")?.trim() ?: ""
        if (slug.isEmpty()) {
            throw BadRequest()
        }

        val pageWithSlug = mediaPageDao.getBySlug(slug, sqlClient)
        if (pageWithSlug != null && pageWithSlug.id != id) {
            throw SlugAlreadyExists()
        }

        val title = data.getString("title")?.trim() ?: ""
        if (title.isEmpty()) {
            throw BadRequest()
        }

        val updatedPage = MediaPage(
            id = id,
            title = title,
            slug = slug,
            description = data.getString("description") ?: "",
            active = data.getBoolean("active"),
            registerToThemeNav = data.getBoolean("registerToThemeNav"),
            sortOrder = existingPage.sortOrder,
            createdAt = existingPage.createdAt,
            updatedAt = System.currentTimeMillis()
        )

        mediaPageDao.update(updatedPage, sqlClient)

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            UpdatedMediaPageLog(userId, username, plugin.pluginId, title),
            sqlClient
        )

        return Successful()
    }
}
