package com.mygdx.game

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.mygdx.game.views.StartView
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.AndroidApplications
import org.hexworks.zircon.api.application.AppConfig

class AndroidLauncher: AndroidApplication() {
    private val tileset = CP437TilesetResources.rexPaint20x20()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = AndroidApplicationConfiguration()
        config.useGL30 = true

        val game = AndroidApplications.startAndroidGameWithApplication(AppConfig.newBuilder()
                .withDefaultTileset(tileset)
                .withSize(60, 30)
                .withDebugMode(true)
                .build(), context)

        initialize(game, config)

        val startView = StartView(game.libgdxApplication.tileGrid)
        startView.dock()

    }
}