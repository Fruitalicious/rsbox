package io.rsbox.server.model.entity

import io.rsbox.api.world.Tile
import io.rsbox.server.Launcher

/**
 * @author Kyle Escobar
 */

open class Entity : io.rsbox.api.entity.Entity {
    lateinit var tile: Tile

    final override val world: io.rsbox.api.world.World = Launcher.server.world

    final override val server: io.rsbox.api.Server = Launcher.server
}