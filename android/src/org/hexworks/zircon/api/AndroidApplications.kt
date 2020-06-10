package org.hexworks.zircon.api

import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.application.Application
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.internal.application.AndroidApplication
import org.hexworks.zircon.internal.application.AndroidGame

object AndroidApplications {

    /**
     * Builds a new [Application] using the given `appConfig`.
     */
    @JvmStatic
    @JvmOverloads
    fun buildApplication(appConfig: AppConfig = AppConfig.defaultConfiguration()): AndroidApplication {
        return makeLibgdxGame(appConfig).libgdxApplication
    }

    /**
     * Builds and starts a new [Application] from the given `appConfig`.
     */
    @JvmStatic
    @JvmOverloads
    fun startApplication(appConfig: AppConfig = AppConfig.defaultConfiguration()): AndroidApplication {
        with(makeLibgdxGame(appConfig)) {
            start()
            return this.libgdxApplication
        }
    }

    /**
     * Builds and starts a new [Application] from the given `appConfig`.
     */
    @JvmStatic
    @JvmOverloads
    fun startAndroidGame(appConfig: AppConfig = AppConfig.defaultConfiguration()): AndroidGame {
        with(makeLibgdxGame(appConfig)) {
            start()
            return this
        }
    }


    /**
     * Builds JUST a new [Application], not a libgdx game
     */
    @JvmStatic
    @JvmOverloads
    fun buildRawApplication(appConfig: AppConfig = AppConfig.defaultConfiguration()): AndroidApplication {
        return AndroidApplication(appConfig)
    }

    /**
     * Starts JUST a new [Application], not a libgdx game
     */
    @JvmStatic
    @JvmOverloads
    fun startRawApplication(appConfig: AppConfig = AppConfig.defaultConfiguration()): AndroidApplication {
        return AndroidApplication(appConfig).also {
            it.start()
        }
    }

    /**
     * Builds and starts a new [Application] and returns its [TileGrid].
     */
    @JvmStatic
    @JvmOverloads
    fun startTileGrid(appConfig: AppConfig = AppConfig.defaultConfiguration()): TileGrid {
        val maxTries = 10
        var currentTryCount = 0
        val game = makeLibgdxGame(appConfig)
        game.start()
        var notInitialized = true
        while (notInitialized) {
            try {
                game.libgdxApplication
                notInitialized = false
            } catch (e: Exception) {
                if (currentTryCount >= maxTries) {
                    throw e
                } else {
                    currentTryCount++
                    Thread.sleep(1000)
                }
            }
        }
        return game.libgdxApplication.tileGrid
    }

    /**
     * Creates a [AndroidGame] that contains a built and started [Application]
     *
     * Note that the Zircon `appConfig` fields title, size, and fpsLimit
     * will override the Libgdx `LwjglApplicationConfiguration` fields
     * title, width and height, and foregroundFPS respectively.
     */
    @JvmStatic
    private fun makeLibgdxGame(appConfig: AppConfig = AppConfig.defaultConfiguration()): AndroidGame {
        return AndroidGame.build(appConfig)
    }
}
