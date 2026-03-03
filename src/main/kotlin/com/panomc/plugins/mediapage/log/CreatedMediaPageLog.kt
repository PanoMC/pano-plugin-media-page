package com.panomc.plugins.mediapage.log

import com.panomc.platform.db.model.PluginActivityLog
import io.vertx.core.json.JsonObject

class CreatedMediaPageLog(
    userId: Long,
    username: String,
    pluginId: String,
    title: String,
) : PluginActivityLog(
    userId = userId,
    pluginId = pluginId,
    details = JsonObject().put("title", title).put("username", username)
)
