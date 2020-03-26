package org.hexworks.zircon.internal.application

import android.content.Context
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import org.hexworks.cobalt.logging.api.LoggerFactory
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.internal.listeners.ZirconInputListener

class AndroidGame(private val appConfig: AppConfig,
                  private val androidContext: Context,
                  private var started: Boolean = false) : Game() {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val tileset = appConfig.defaultTileset

    private lateinit var batch: SpriteBatch

    val libgdxApplication = AndroidApplication(appConfig)

    fun start() {
        //TODO this should be refactored to be in an AndroidApplication(GDX) I guess
//        if(!started) {
//            LwjglApplication(this, libgdxConfig)
//        }
        started = true
    }

    override fun create() {
        logger.info("Creating LibgdxGame...")

        batch = SpriteBatch()
        batch.enableBlending()


        libgdxApplication.start()
        val tileGrid = libgdxApplication.tileGrid

        Gdx.input.inputProcessor = ZirconInputListener(
                fontWidth = tileset.width,
                fontHeight = tileset.height,
                tileGrid = tileGrid,
                androidContext = androidContext)
    }

    override fun render() {
        super.render()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        libgdxApplication.render()
    }

    override fun dispose() {
        batch.dispose()
    }

    companion object {
        fun build(appConfig: AppConfig = AppConfig.defaultConfiguration(),
                  androidContext: Context): AndroidGame {
            return AndroidGame(appConfig, androidContext)
        }
    }


}
