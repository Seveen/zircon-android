package com.mygdx.game.views

import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.ComponentDecorations.box
import org.hexworks.zircon.api.ComponentDecorations.shadow
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.view.base.BaseView

class StartView(grid: TileGrid): BaseView(grid, ColorThemes.adriftInDreams()) {

    val panel = Components.panel()
            .withSize(40, 20)
            .withDecorations(box(title = "Hello Android"), shadow())
            .withAlignmentWithin(grid, ComponentAlignment.CENTER)
            .build()

    val button = Components.button()
            .withText("Hi there...")
            .withAlignmentWithin(panel, ComponentAlignment.CENTER)
            .build()

    val button2 = Components.button()
            .withText("Hi there...")
            .withAlignmentWithin(panel, ComponentAlignment.BOTTOM_CENTER)
            .build()

    init {
        panel.addComponent(button)
        screen.addComponent(panel)
    }

}