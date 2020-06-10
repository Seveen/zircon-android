package com.mygdx.game.views

import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.GameComponents
import org.hexworks.zircon.api.builder.game.GameAreaBuilder
import org.hexworks.zircon.api.color.ANSITileColor
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.data.*
import org.hexworks.zircon.api.game.GameArea
import org.hexworks.zircon.api.game.ProjectionMode
import org.hexworks.zircon.api.game.base.BaseGameArea
import org.hexworks.zircon.api.graphics.Symbols
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.uievent.MouseEventType
import org.hexworks.zircon.api.uievent.Processed
import org.hexworks.zircon.api.view.base.BaseView
import org.hexworks.zircon.internal.data.DefaultBlock

class GameView(grid: TileGrid): BaseView(grid, ColorThemes.adriftInDreams()) {

    private val FLOOR = Tile.newBuilder()
            .withCharacter(Symbols.INTERPUNCT)
            .withForegroundColor(ANSITileColor.YELLOW)
            .buildCharacterTile()

    private val WALL = Tile.newBuilder()
            .withCharacter('#')
            .withForegroundColor(TileColor.fromString("#999999"))
            .buildCharacterTile()

    private val PLAYER = Tile.newBuilder()
            .withCharacter('@')
            .withForegroundColor(TileColor.fromString("#FFFFFF"))
            .buildCharacterTile()

    private var playerPosition = Position3D.create(10, 10, 0)
    private var underPlayer: Block<Tile>? = null

    private val VISIBLE_SIZE = Size3D.create(28, 16, 10)
    private val ACTUAL_SIZE = Size3D.create(100, 100, 100)

    private val touchLeft = screen.size.width / 3
    private val touchRight = (screen.size.width / 3) * 2

    private val touchTop = screen.size.height / 3
    private val touchBottom = (screen.size.height / 3) * 2

    private val area = CustomGameArea(VISIBLE_SIZE, ACTUAL_SIZE)

    private val game = Components.gameComponent<Tile, Block<Tile>>()
            .withGameArea(area)
            .withAlignmentWithin(grid, ComponentAlignment.CENTER)
            .build()

    override fun onDock() {
        makeCaves(area)
        addPlayer(area)

        screen.addComponent(game)
        screen.handleMouseEvents(MouseEventType.MOUSE_CLICKED) { event, _ ->
            val x = when  {
                event.position.x <= touchLeft -> -1
                event.position.x >= touchRight -> 1
                else -> 0
            }
            val y = when  {
                event.position.y <= touchTop -> -1
                event.position.y >= touchBottom -> 1
                else -> 0
            }
            movePlayer(area, x, y)
            Processed
        }
    }

    private fun makeCaves(gameArea: GameArea<Tile, Block<Tile>>, smoothTimes: Int = 8) {
        val width = gameArea.actualSize.xLength
        val height = gameArea.actualSize.yLength
        var tiles: MutableMap<Position, Tile> = mutableMapOf()
        gameArea.actualSize.to2DSize().fetchPositions().forEach { pos ->
            tiles[pos] = if (Math.random() < 0.5) FLOOR else WALL
        }
        val newTiles: MutableMap<Position, Tile> = mutableMapOf()
        for (time in 0 until smoothTimes) {

            for (x in 0 until width) {
                for (y in 0 until height) {
                    var floors = 0
                    var rocks = 0

                    for (ox in -1..1) {
                        for (oy in -1..1) {
                            if (x + ox < 0 || x + ox >= width || y + oy < 0
                                    || y + oy >= height)
                                continue

                            if (tiles[Position.create(x + ox, y + oy)] === FLOOR)
                                floors++
                            else
                                rocks++
                        }
                    }
                    newTiles[Position.create(x, y)] = if (floors >= rocks) FLOOR else WALL
                }
            }
            tiles = newTiles
        }
        tiles.forEach { (pos, tile) ->
            val pos3D = pos.to3DPosition(0)
            gameArea.setBlockAt(pos3D, Block.newBuilder<Tile>()
                    .withContent(tile)
                    .withEmptyTile(Tile.empty())
                    .build())
        }
    }

    private fun addPlayer(gameArea: GameArea<Tile, Block<Tile>>) {
        underPlayer = gameArea.fetchBlockAt(playerPosition).get()
        gameArea.setBlockAt(playerPosition, Block.newBuilder<Tile>()
                .withContent(PLAYER)
                .withEmptyTile(Tile.empty())
                .build())
    }

    private fun movePlayer(gameArea: GameArea<Tile, Block<Tile>>, ox: Int, oy: Int) {
        val next = playerPosition.plus(Position3D.create(ox, oy, 0))

        gameArea.setBlockAt(playerPosition, underPlayer!!)

        underPlayer = gameArea.fetchBlockAt(next).get()

        gameArea.setBlockAt(next, Block.newBuilder<Tile>()
                .withContent(PLAYER)
                .withEmptyTile(Tile.empty())
                .build())

        playerPosition = next
    }
}

class CustomGameArea(
        visibleSize: Size3D,
        actualSize: Size3D
) : BaseGameArea<Tile, Block<Tile>>(
        initialVisibleSize = visibleSize,
        initialActualSize = actualSize
)

