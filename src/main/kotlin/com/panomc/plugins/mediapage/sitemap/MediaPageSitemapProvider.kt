package com.panomc.plugins.mediapage.sitemap

import com.panomc.platform.api.SitemapEntry
import com.panomc.platform.api.SitemapProvider
import com.panomc.plugins.mediapage.db.dao.MediaPageDao
import io.vertx.sqlclient.SqlClient
import org.springframework.stereotype.Component

/** The active media pages, for `GET /api/v1/sitemap`. */
@Component
class MediaPageSitemapProvider(private val mediaPageDao: MediaPageDao) : SitemapProvider {
    override suspend fun entries(sqlClient: SqlClient): List<SitemapEntry> =
        mediaPageDao.getAllActive(sqlClient)
            .map { page -> SitemapEntry("pano-plugin-media-page:media-page", mapOf("slug" to page.slug), page.updatedAt) }
}
