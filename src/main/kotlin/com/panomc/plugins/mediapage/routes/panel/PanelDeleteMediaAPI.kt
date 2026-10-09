package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaItemDao
import com.panomc.plugins.mediapage.log.DeletedMediaLog
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Parameters.param
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.numberSchema
import java.io.File

@Endpoint
class PanelDeleteMediaAPI(
    private val plugin: MediaPagePlugin,
    private val mediaItemDao: MediaItemDao
) : PanelApi() {
    override val paths = listOf(Path("/media-items/:id", RouteType.DELETE))

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

        val item = mediaItemDao.getById(id, sqlClient) ?: throw NotFound()

        // Delete physical files from plugin data folder
        try {
            if (item.filePath.isNotEmpty()) {
                val file = File(plugin.uploadsDir, item.filePath)
                if (file.exists()) file.delete()
            }

            if (item.thumbnailPath.isNotEmpty()) {
                val thumbFile = File(plugin.thumbnailsDir, item.thumbnailPath)
                if (thumbFile.exists()) thumbFile.delete()
            }
        } catch (e: Exception) {
            plugin.logger.warn("Failed to delete media files for item $id: ${e.message}")
        }

        mediaItemDao.deleteById(id, sqlClient)

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            DeletedMediaLog(userId, username, plugin.pluginId, item.originalFileName),
            sqlClient
        )

        return Successful()
    }
}
