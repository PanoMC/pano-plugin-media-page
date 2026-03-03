package com.panomc.plugins.mediapage.db.model

import com.panomc.platform.db.DBEntity

open class MediaPage(
    val id: Long = -1,
    val title: String = "",
    val slug: String = "",
    val description: String = "",
    val active: Boolean = true,
    val registerToThemeNav: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) : DBEntity()
