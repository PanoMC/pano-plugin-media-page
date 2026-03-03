package com.panomc.plugins.mediapage.log

import com.panomc.platform.db.model.PluginActivityLog
import io.vertx.core.json.JsonObject

class DeletedMediaLog(
    userId: Long,
    username: String,
    pluginId: String,
    fileName: String,
) : PluginActivityLog(
    userId = userId,
    pluginId = pluginId,
    details = JsonObject().put("fileName", fileName).put("username", username)
)
