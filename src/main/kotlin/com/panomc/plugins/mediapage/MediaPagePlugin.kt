package com.panomc.plugins.mediapage

import com.panomc.platform.api.PanoPlugin
import com.panomc.platform.api.PluginDatabaseManager
import com.panomc.platform.setup.SetupManager
import java.io.File

class MediaPagePlugin : PanoPlugin() {
    private val pluginDatabaseManager by lazy {
        applicationContext.getBean(PluginDatabaseManager::class.java)
    }

    private val setupManager by lazy {
        applicationContext.getBean(SetupManager::class.java)
    }

    val uploadsDir: File by lazy {
        File(pluginDataFolder, "uploads")
    }

    val thumbnailsDir: File by lazy {
        File(pluginDataFolder, "thumbnails")
    }

    private var isInitialized = false

    override suspend fun onStart() {
        logger.info("Starting...")

        startPlugin()
    }

    suspend fun startPlugin() {
        if (isInitialized) return

        if (!setupManager.isSetupDone()) {
            logger.info("Setup is not finished, waiting for setup completion...")
            return
        }

        isInitialized = true

        pluginDatabaseManager.initialize(this)

        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs()
        }

        if (!thumbnailsDir.exists()) {
            thumbnailsDir.mkdirs()
        }

        logger.info("Started!")
    }

    override suspend fun onEnable() {
        logger.info("Enabled!")
    }

    override suspend fun onDisable() {
        isInitialized = false
    }

    override suspend fun onUninstall() {
        logger.info("Uninstalling...")
        pluginDatabaseManager.uninstall(this)

        if (uploadsDir.exists()) {
            uploadsDir.deleteRecursively()
        }

        if (thumbnailsDir.exists()) {
            thumbnailsDir.deleteRecursively()
        }
    }
}
