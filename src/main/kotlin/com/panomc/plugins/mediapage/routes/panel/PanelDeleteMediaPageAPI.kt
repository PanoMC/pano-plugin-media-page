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
import com.panomc.plugins.mediapage.log.DeletedMediaPageLog
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Parameters.param
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.numberSchema

@Endpoint
class PanelDeleteMediaPageAPI(
    private val plugin: MediaPagePlugin,
    private val mediaPageDao: MediaPageDao,
    private val mediaCategoryDao: MediaCategoryDao,
    private val mediaItemDao: MediaItemDao
) : PanelApi() {
    override val paths = listOf(Path("/media-pages/:id", RouteType.DELETE))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler {
        return ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(param("id", numberSchema()))
            .build()
    }

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val parameters = getParameters(context)
        val id = parameters.pathParameter("id").long
        val sqlClient = databaseManager.getSqlClient()

        val mediaPage = mediaPageDao.getById(id, sqlClient) ?: throw NotFound()

        // Delete all media items in this page's categories (with physical files)
        val categories = mediaCategoryDao.getByPageId(id, sqlClient)
        categories.forEach { category ->
            val items = mediaItemDao.getByCategoryId(category.id, sqlClient)
            items.forEach { item ->
                try {
                    if (item.filePath.isNotEmpty()) {
                        val file = java.io.File(plugin.uploadsDir, item.filePath)
                        if (file.exists()) file.delete()
                    }
                    if (item.thumbnailPath.isNotEmpty()) {
                        val thumbFile = java.io.File(plugin.thumbnailsDir, item.thumbnailPath)
                        if (thumbFile.exists()) thumbFile.delete()
                    }
                } catch (_: Exception) {}
            }
            mediaItemDao.deleteByCategoryId(category.id, sqlClient)
        }

        // Delete all categories of this page
        mediaCategoryDao.deleteByPageId(id, sqlClient)

        // Delete the page itself
        mediaPageDao.deleteById(id, sqlClient)

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            DeletedMediaPageLog(userId, username, plugin.pluginId, mediaPage.title),
            sqlClient
        )

        return Successful()
    }
}
