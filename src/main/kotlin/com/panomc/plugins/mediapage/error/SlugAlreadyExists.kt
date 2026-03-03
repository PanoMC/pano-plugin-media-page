package com.panomc.plugins.mediapage.error

import com.panomc.platform.model.Error

class SlugAlreadyExists(
    statusMessage: String = "",
    extras: Map<String, Any?> = mapOf()
) : Error(422, statusMessage, extras)
