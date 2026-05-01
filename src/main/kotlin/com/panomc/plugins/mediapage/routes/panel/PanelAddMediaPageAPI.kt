package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.BadRequest
import com.panomc.platform.model.*
import com.panomc.plugins.license.LicenseGuard
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaCategoryDao
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import com.panomc.plugins.mediapage.db.model.MediaCategory
import com.panomc.plugins.mediapage.db.model.MediaPage
import com.panomc.plugins.mediapage.error.SlugAlreadyExists
import com.panomc.plugins.mediapage.log.CreatedMediaPageLog
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class PanelAddMediaPageAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao,
    private val mediaCategoryDao: MediaCategoryDao
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/media-pages", RouteType.POST))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun bodyHandler(): Handler<RoutingContext> = BodyHandler.create()

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .body(
                Bodies.json(
                    objectSchema()
                        .requiredProperty("title", stringSchema())
                        .requiredProperty("slug", stringSchema())
                        .optionalProperty("description", stringSchema())
                        .requiredProperty("active", booleanSchema())
                        .requiredProperty("registerToThemeNav", booleanSchema())
                        .optionalProperty("categories", arraySchema().items(
                            objectSchema()
                                .requiredProperty("title", stringSchema())
                                .optionalProperty("description", stringSchema())
                        ))
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        // Anti-tamper: re-assert license at a critical write path.
        LicenseGuard.assert(plugin)

        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject
        val sqlClient = databaseManager.getSqlClient()

        val slug = data.getString("slug")?.trim() ?: ""
        if (slug.isEmpty()) {
            throw BadRequest()
        }

        if (mediaPageDao.getBySlug(slug, sqlClient) != null) {
            throw SlugAlreadyExists()
        }

        val title = data.getString("title")?.trim() ?: ""
        if (title.isEmpty()) {
            throw BadRequest()
        }

        val mediaPage = MediaPage(
            title = title,
            slug = slug,
            description = data.getString("description") ?: "",
            active = data.getBoolean("active"),
            registerToThemeNav = data.getBoolean("registerToThemeNav")
        )

        val id = mediaPageDao.add(mediaPage, sqlClient)

        // Create categories if provided
        val categoriesArray = data.getJsonArray("categories")
        if (categoriesArray != null && !categoriesArray.isEmpty) {
            categoriesArray.forEachIndexed { index, catObj ->
                val cat = catObj as io.vertx.core.json.JsonObject
                val category = MediaCategory(
                    pageId = id,
                    title = cat.getString("title"),
                    description = cat.getString("description") ?: "",
                    sortOrder = index
                )
                mediaCategoryDao.add(category, sqlClient)
            }
        }

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            CreatedMediaPageLog(userId, username, plugin.pluginId, title),
            sqlClient
        )

        return Successful(mapOf("id" to id))
    }
}
