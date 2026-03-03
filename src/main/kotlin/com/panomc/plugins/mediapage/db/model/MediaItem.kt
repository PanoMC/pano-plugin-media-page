package com.panomc.plugins.mediapage.db.model

import com.panomc.platform.db.DBEntity

open class MediaItem(
    val id: Long = -1,
    val categoryId: Long = -1,
    val type: String = "IMAGE",
    val title: String = "",
    val description: String = "",
    val filePath: String = "",
    val thumbnailPath: String = "",
    val originalFileName: String = "",
    val mimeType: String = "",
    val fileSize: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val duration: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) : DBEntity()
