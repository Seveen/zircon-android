package org.hexworks.zircon.internal.renderer

import android.os.Debug
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import org.hexworks.cobalt.databinding.api.extension.toProperty
import org.hexworks.cobalt.datatypes.Maybe
import org.hexworks.zircon.api.Maybes
import org.hexworks.zircon.api.application.CursorStyle
import org.hexworks.zircon.api.behavior.TilesetOverride
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.tileset.Tileset
import org.hexworks.zircon.internal.RunTimeStats
import org.hexworks.zircon.internal.config.RuntimeConfig
import org.hexworks.zircon.internal.data.LayerState
import org.hexworks.zircon.internal.data.PixelPosition
import org.hexworks.zircon.internal.grid.InternalTileGrid
import org.hexworks.zircon.internal.tileset.AndroidTilesetLoader


//TODO: remove sprite creation from the render loop
//TODO: object pooling?
//TODO: remove reflection-using methods. (e.g. Position.plus())
//TODO: (last resort) tilegrid datastructure not fast enough, iteration is slow

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class AndroidRenderer(private val grid: InternalTileGrid,
                      private val debug: Boolean = false) : Renderer {

    override val isClosed = false.toProperty()

    private val config = RuntimeConfig.config
    private lateinit var batch: SpriteBatch
    private var backSprite = Sprite()
    private lateinit var cursorRenderer: ShapeRenderer
    private val tilesetLoader = AndroidTilesetLoader()
    private var blinkOn = true
    private var timeSinceLastBlink: Float = 0f

    private lateinit var backgroundTexture: Texture
    private var backgroundWidth: Int = 0
    private var backgroundHeight: Int = 0

    override fun create() {
        batch = SpriteBatch().apply {
            val camera = OrthographicCamera()
            camera.setToOrtho(true)
            projectionMatrix = camera.combined
        }

        cursorRenderer = ShapeRenderer()
        val whitePixmap = Pixmap(grid.widthInPixels, grid.heightInPixels, Pixmap.Format.RGBA8888)
        whitePixmap.setColor(Color.WHITE)
        whitePixmap.fill()
        backgroundTexture = Texture(whitePixmap)

        backgroundWidth = whitePixmap.width / grid.width
        backgroundHeight = whitePixmap.height / grid.height

        whitePixmap.dispose()
    }

    override fun render() {
        if (debug) {
            RunTimeStats.addTimedStatFor("debug.render.time") {
                doRender(Gdx.app.graphics.deltaTime)
            }
        } else doRender(Gdx.app.graphics.deltaTime)
    }

    override fun close() {
        isClosed.value = true
        batch.dispose()
    }

    private fun doRender(delta: Float) {
        handleBlink(delta)

        batch.begin()

        grid.fetchLayerStates().forEach { state ->
            renderTiles(
                    batch = batch,
                    state = state,
                    tileset = tilesetLoader.loadTilesetFrom(grid.tileset),
                    offset = state.position.toPixelPosition(grid.tileset)
            )
        }
        batch.end()
        cursorRenderer.projectionMatrix = batch.projectionMatrix
        if (shouldDrawCursor()) {
            grid.getTileAt(grid.cursorPosition).map {
                drawCursor(cursorRenderer, it, grid.cursorPosition)
            }
        }
    }

    private fun renderTiles(batch: SpriteBatch,
                            state: LayerState,
                            tileset: Tileset<SpriteBatch>,
                            offset: PixelPosition = PixelPosition(0, 0)) {

//        Debug.startMethodTracing("test.trace")
        state.tiles.forEach { (pos, tile) ->
            val actualX = pos.x + state.position.x
            val actualY = pos.y + state.position.y

            if (tile !== Tile.empty()) {
                val actualTile =
                        if (tile.isBlinking /*&& blinkOn*/) {
                            tile.withBackgroundColor(tile.foregroundColor)
                                    .withForegroundColor(tile.backgroundColor)
                        } else {
                            tile
                        }
                val actualTileset: Tileset<SpriteBatch> =
                        if (actualTile is TilesetOverride) {
                            tilesetLoader.loadTilesetFrom(actualTile.tileset)
                        } else {
                            tileset
                        }

                drawBack(
                        tile = actualTile,
                        surface = batch,
                        x = actualX * actualTileset.width.toFloat(),
                        y = actualY * actualTileset.height.toFloat()
                )
            }
        }
//        Debug.stopMethodTracing()

//        Debug.startMethodTracing("test2.trace")
        state.tiles.forEach { (pos, tile) ->
            val actualX = pos.x + state.position.x
            val actualY = pos.y + state.position.y

            if (tile !== Tile.empty()) {
                val actualTile =
                        if (tile.isBlinking /*&& blinkOn*/) {
                            tile.withBackgroundColor(tile.foregroundColor)
                                    .withForegroundColor(tile.backgroundColor)
                        } else {
                            tile
                        }
                val actualTileset: Tileset<SpriteBatch> =
                        if (actualTile is TilesetOverride) {
                            tilesetLoader.loadTilesetFrom(actualTile.tileset)
                        } else {
                            tileset
                        }
                val pixelPos = Position.create(actualX * actualTileset.width, actualY * actualTileset.height)
                actualTileset.drawTile(
                        tile = actualTile,
                        surface = batch,
                        position = pixelPos
                )
            }
        }
//        Debug.stopMethodTracing()

    }

    private fun  drawBack(tile: Tile, surface: SpriteBatch, x: Float, y: Float) {
        backSprite.texture = backgroundTexture
        backSprite.setSize(backgroundWidth.toFloat(), backgroundHeight.toFloat())
        backSprite.setOrigin(0f, 0f)
        backSprite.setOriginBasedPosition(x, y)
        backSprite.flip(false, true)
        backSprite.color = Color(
                tile.backgroundColor.red.toFloat() / 255,
                tile.backgroundColor.green.toFloat() / 255,
                tile.backgroundColor.blue.toFloat() / 255,
                tile.backgroundColor.alpha.toFloat() / 255
        )
        backSprite.draw(surface)
    }

    private fun handleBlink(delta: Float) {
        timeSinceLastBlink += delta
        if (timeSinceLastBlink > config.blinkLengthInMilliSeconds) {
            blinkOn = !blinkOn
        }
    }

    private fun drawCursor(shapeRenderer: ShapeRenderer, character: Tile, position: Position) {
        val tileWidth = grid.tileset.width
        val tileHeight = grid.tileset.height
        val x = (position.x * tileWidth).toFloat()
        val y = (position.y * tileHeight).toFloat()
        val cursorColor = colorToGDXColor(config.cursorColor)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = cursorColor
        when (config.cursorStyle) {
            CursorStyle.USE_CHARACTER_FOREGROUND -> {
                if (blinkOn) {
                    shapeRenderer.color = colorToGDXColor(character.foregroundColor)
                    shapeRenderer.rect(x, y, tileWidth.toFloat(), tileHeight.toFloat())
                }
            }
            CursorStyle.FIXED_BACKGROUND -> shapeRenderer.rect(x, y, tileWidth.toFloat(), tileHeight.toFloat())
            CursorStyle.UNDER_BAR -> shapeRenderer.rect(x, y + tileHeight - 3, tileWidth.toFloat(), 2.0f)
            CursorStyle.VERTICAL_BAR -> shapeRenderer.rect(x, y + 1, 2.0f, tileHeight - 2.0f)
        }
        shapeRenderer.end()
    }

    private fun shouldDrawCursor(): Boolean {
        return grid.isCursorVisible &&
                (config.isCursorBlinking.not() || config.isCursorBlinking && blinkOn)
    }

    private fun colorToGDXColor(color: TileColor): Color {
        return Color(
                color.red / 255.0f,
                color.green / 255.0f,
                color.blue / 255.0f,
                color.alpha / 255.0f
        )
    }

    fun TileColor.toGdxColor(): Color {
        return Color(
                this.red / 255.0f,
                this.green / 255.0f,
                this.blue / 255.0f,
                this.alpha / 255.0f
        )
    }
}
