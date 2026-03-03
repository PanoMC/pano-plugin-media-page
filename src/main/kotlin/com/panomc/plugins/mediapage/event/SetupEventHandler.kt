package com.panomc.plugins.mediapage.event

import com.panomc.platform.api.annotation.EventListener
import com.panomc.platform.api.event.SetupEventListener
import com.panomc.plugins.mediapage.MediaPagePlugin
import org.pf4j.PluginState

@EventListener
class SetupEventHandler(private val plugin: MediaPagePlugin) : SetupEventListener {
    private val logger by lazy {
        plugin.logger
    }

    override suspend fun onSetupFinished() {
        if (plugin.pluginState == PluginState.STARTED) {
            logger.info("Setup finished! Initializing plugin...")

            plugin.startPlugin()
        }
    }
}
