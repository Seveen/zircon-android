package com.mygdx.game

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.mygdx.game.views.StartView
import org.hexworks.zircon.api.AndroidApplications
import org.hexworks.zircon.api.AndroidLauncher
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.application.AppConfig

class MyAndroidLauncher: AndroidLauncher() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        initializeGame()
        StartView(grid).dock()
    }
}