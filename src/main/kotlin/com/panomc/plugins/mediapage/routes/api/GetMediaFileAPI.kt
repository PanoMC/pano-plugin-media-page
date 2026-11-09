package com.panomc.plugins.mediapage.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.model.*
import com.panomc.plugins.mediapage.util.MediaSniffer
import com.panomc.plugins.mediapage.MediaPagePlugin
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Parameters
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.stringSchema
import java.io.File
import com.panomc.platform.schema.EndpointDoc
import io.vertx.json.schema.common.dsl.Schemas.*
import com.panomc.platform.error.NotFound

@Endpoint
class GetMediaFileAPI(
    private val plugin: MediaPagePlugin
) : Api() {
    override val paths = listOf(Path("/media/file/:fileName", RouteType.GET))

    override val doc = EndpointDoc(
        summary = "An uploaded media file or its thumbnail.",
        tag = "media-pages",
        binary = true,
        errors = listOf(NotFound::class)
    )

    companion object {
        private const val CACHE_TTL_SECONDS = 7 * 24 * 60 * 60 // 1 week

        private val SAFE_FILE_NAME = Regex("^[A-Za-z0-9._-]+$")
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(Parameters.param("fileName", stringSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result? {
        val parameters = getParameters(context)
        val fileName = parameters.pathParameter("fileName").string

        // Security: only a plain file name directly inside the uploads or thumbnails dir, checked before the disk is
        // touched, and every miss answers the same 404 so nothing can be learned about other paths on the server.
        if (!SAFE_FILE_NAME.matches(fileName) || fileName.contains("..")) {
            context.response().setStatusCode(404).end()
            return null
        }

        // Check uploads dir first, then thumbnails dir
        val file = listOf(plugin.uploadsDir.canonicalFile, plugin.thumbnailsDir.canonicalFile)
            .map { dir -> dir to File(dir, fileName) }
            .firstOrNull { (dir, candidate) -> candidate.canonicalFile.parentFile == dir && candidate.isFile }
            ?.second

        if (file == null) {
            context.response().setStatusCode(404).end()
            return null
        }

        val mimeType = MediaSniffer.mimeTypeOf(file.extension)

        val response = context.response()
        // Only image and video types are served inline; anything else (files from before uploads were checked) is a
        // download that the browser never renders.
        if (mimeType != null) {
            response.putHeader("Content-Type", mimeType)
        } else {
            response.putHeader("Content-Type", "application/octet-stream")
            response.putHeader("Content-Disposition", "attachment")
        }
        response.putHeader("X-Content-Type-Options", "nosniff")
        response.putHeader("Content-Security-Policy", "default-src 'none'; sandbox")
        response.putHeader("Cache-Control", "public, max-age=$CACHE_TTL_SECONDS, immutable")

        try {
            response.sendFile(file.absolutePath)
        } catch (_: Exception) {}

        return null
    }
}
