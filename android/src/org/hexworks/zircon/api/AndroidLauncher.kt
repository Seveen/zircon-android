package org.hexworks.zircon.api

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.internal.application.AndroidGame

open class AndroidLauncher: AndroidApplication() {
    open var tileset = CP437TilesetResources.hack64x64()
    open lateinit var config: AndroidApplicationConfiguration
    open lateinit var game: AndroidGame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = AndroidApplicationConfiguration()

        game = AndroidApplications.startAndroidGame(
                AppConfig.newBuilder()
                        .withDefaultTileset(tileset)
                        .withSize(28, 16)
                        .withDebugMode(true)
                        .build(),
                context)
    }

    fun initializeGame() {
        initialize(game, config)
    }
}