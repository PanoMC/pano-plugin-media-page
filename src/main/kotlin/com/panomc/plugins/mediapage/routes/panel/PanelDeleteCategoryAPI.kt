package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.db.dao.MediaCategoryDao
import com.panomc.plugins.mediapage.db.dao.MediaItemDao
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Parameters.param
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.numberSchema

@Endpoint
class PanelDeleteCategoryAPI(
    private val plugin: MediaPagePlugin,
    private val mediaCategoryDao: MediaCategoryDao,
    private val mediaItemDao: MediaItemDao
) : PanelApi() {
    override val paths = listOf(Path("/media-categories/:id", RouteType.DELETE))

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

        mediaCategoryDao.getById(id, sqlClient) ?: throw NotFound()

        // Delete all media items and their files in this category
        val items = mediaItemDao.getByCategoryId(id, sqlClient)
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
        mediaItemDao.deleteByCategoryId(id, sqlClient)

        // Delete the category
        mediaCategoryDao.deleteById(id, sqlClient)

        return Successful()
    }
}
