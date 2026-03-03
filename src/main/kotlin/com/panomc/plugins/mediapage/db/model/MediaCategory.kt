package com.panomc.plugins.mediapage.db.model

import com.panomc.platform.db.DBEntity

open class MediaCategory(
    val id: Long = -1,
    val pageId: Long = -1,
    val title: String = "",
    val description: String = "",
    val visible: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) : DBEntity()
