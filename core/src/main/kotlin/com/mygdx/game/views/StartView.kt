package com.mygdx.game.views

import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.ComponentDecorations.box
import org.hexworks.zircon.api.ComponentDecorations.shadow
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.view.base.BaseView

class StartView(grid: TileGrid): BaseView(grid, ColorThemes.adriftInDreams()) {

    private val panel = Components.panel()
            .withSize(20, 10)
            .withDecorations()
            .withAlignmentWithin(grid, ComponentAlignment.CENTER)
            .build()

    private val label = Components.label()
            .withText("Zircon Android")
            .withAlignmentWithin(panel, ComponentAlignment.CENTER)
            .build()

    private val button = Components.button()
            .withText("Start")
            .withAlignmentAround(label, ComponentAlignment.BOTTOM_CENTER)
            .build().apply {
                onActivated {
                    GameView(grid).dock()
                }
            }

    init {
        panel.addComponent(label)
        panel.addComponent(button)
        screen.addComponent(panel)
    }
}