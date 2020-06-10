package org.hexworks.zircon.api

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.internal.application.AndroidGame
import org.hexworks.zircon.internal.grid.InternalTileGrid

open class AndroidLauncher: AndroidApplication() {
    var androidConfig = AndroidApplicationConfiguration()
    var config = AppConfig.newBuilder()
            .withDefaultTileset(CP437TilesetResources.hack64x64())
            .withSize(28, 16)
            .withDebugMode(true)
            .build()
    lateinit var game: AndroidGame
    lateinit var grid: InternalTileGrid

    fun initializeGame() {
        game = AndroidApplications.startAndroidGame(config)
        grid = game.libgdxApplication.tileGrid
        initialize(game, androidConfig)
    }
}