package com.panomc.plugins.mediapage.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.MediaPagePlugin
import com.panomc.plugins.mediapage.util.ImageDecoding
import com.panomc.plugins.mediapage.util.MediaSniffer
import com.panomc.plugins.mediapage.db.dao.MediaCategoryDao
import com.panomc.plugins.mediapage.db.dao.MediaItemDao
import com.panomc.plugins.mediapage.db.model.MediaItem
import com.panomc.plugins.mediapage.log.UploadedMediaLog
import com.panomc.plugins.mediapage.permission.ManageMediaPagePermission
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Parameters.param
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.numberSchema
import java.awt.image.BufferedImage
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO

@Endpoint
class PanelUploadMediaAPI(
    private val plugin: MediaPagePlugin,
    private val mediaCategoryDao: MediaCategoryDao,
    private val mediaItemDao: MediaItemDao
) : PanelApi() {
    override val paths = listOf(Path("/media-items/upload/:categoryId", RouteType.POST))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun bodyHandler(): Handler<RoutingContext> =
        BodyHandler.create()
            .setDeleteUploadedFilesOnEnd(true)
            .setBodyLimit(100 * 1024 * 1024) // 100MB limit

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(param("categoryId", numberSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageMediaPagePermission(), context)

        val parameters = getParameters(context)
        val categoryId = parameters.pathParameter("categoryId").long
        val sqlClient = databaseManager.getSqlClient()

        mediaCategoryDao.getById(categoryId, sqlClient) ?: throw NotFound()

        val uploads = context.fileUploads()
        if (uploads.isEmpty()) {
            throw com.panomc.platform.error.BadRequest()
        }

        // Read optional title and description from form attributes
        val formTitle = context.request().getFormAttribute("title")?.takeIf { it.isNotBlank() }
        val formDescription = context.request().getFormAttribute("description")?.takeIf { it.isNotBlank() }

        // The type of every file comes from its content; one file that is not an image or video refuses the upload.
        val detectedTypes = uploads.map { MediaSniffer.detect(File(it.uploadedFileName())) ?: throw com.panomc.platform.error.BadRequest() }

        // Image sizes come from the header; an image too big to decode safely refuses the upload.
        val dimensions = uploads.mapIndexed { index, upload ->
            if (detectedTypes[index].type != "IMAGE") {
                return@mapIndexed null
            }

            val size = try {
                ImageDecoding.readDimensions(File(upload.uploadedFileName()))
            } catch (_: Exception) {
                null
            }

            if (size != null && ImageDecoding.isTooLarge(size.first, size.second)) {
                throw com.panomc.platform.error.BadRequest()
            }

            size
        }

        val uploadedItems = mutableListOf<Map<String, Any>>()

        for ((index, upload) in uploads.withIndex()) {
            val detected = detectedTypes[index]
            val originalFileName = upload.fileName()
            val mimeType = detected.mimeType
            val fileSize = upload.size()
            val uploadedFile = File(upload.uploadedFileName())

            // Determine media type
            val type = detected.type

            // Generate unique filename
            val extension = detected.extension
            val uniqueName = "${UUID.randomUUID()}.$extension"

            // Save to plugin data folder
            val destFile = File(plugin.uploadsDir, uniqueName)
            uploadedFile.copyTo(destFile, overwrite = true)

            // Store only the filename, not the full path
            val fileName = uniqueName

            // Generate thumbnail for images
            var thumbnailFileName = ""
            var width = 0
            var height = 0

            val size = dimensions[index]

            if (type == "IMAGE" && size != null) {
                try {
                    width = size.first
                    height = size.second

                    // Decoded with subsampling, so only about the thumbnail's pixels are held in memory.
                    val image: BufferedImage? = ImageDecoding.readSubsampled(destFile, 400)
                    if (image != null) {
                        // Generate thumbnail
                        val thumbWidth = 400
                        val thumbHeight = (thumbWidth.toDouble() / image.width * image.height).toInt().coerceAtLeast(1)

                        val thumbImage = BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB)
                        val g = thumbImage.createGraphics()
                        g.drawImage(image.getScaledInstance(thumbWidth, thumbHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null)
                        g.dispose()

                        val thumbName = "thumb_$uniqueName"
                        val thumbFile = File(plugin.thumbnailsDir, thumbName)
                        // ImageIO has no WebP writer; a WebP thumbnail is stored as JPEG bytes.
                        ImageIO.write(thumbImage, if (extension == "webp") "jpg" else extension, thumbFile)

                        thumbnailFileName = thumbName
                    }
                } catch (e: Exception) {
                    plugin.logger.warn("Failed to generate thumbnail for $originalFileName: ${e.message}")
                }
            }

            // Use form title if provided, otherwise use filename
            val title = formTitle ?: originalFileName.substringBeforeLast('.')
            val description = formDescription ?: ""

            val mediaItem = MediaItem(
                categoryId = categoryId,
                type = type,
                title = title,
                description = description,
                filePath = fileName,
                thumbnailPath = thumbnailFileName,
                originalFileName = originalFileName,
                mimeType = mimeType,
                fileSize = fileSize,
                width = width,
                height = height
            )

            val id = mediaItemDao.add(mediaItem, sqlClient)

            uploadedItems.add(
                mapOf(
                    "id" to id,
                    "type" to type,
                    "title" to title,
                    "filePath" to fileName,
                    "thumbnailPath" to thumbnailFileName,
                    "originalFileName" to originalFileName,
                    "mimeType" to mimeType,
                    "fileSize" to fileSize,
                    "width" to width,
                    "height" to height
                )
            )
        }

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            UploadedMediaLog(
                userId,
                username,
                plugin.pluginId,
                uploads.joinToString(", ") { it.fileName() }
            ),
            sqlClient
        )

        return Successful(mapOf("items" to uploadedItems))
    }
}
