package io.rsbox.engine.model.entity

import io.rsbox.engine.model.world.Tile

/**
 * @author Kyle Escobar
 */

open class Entity : io.rsbox.api.entity.Entity {

    var _tile: Tile = Tile(3221,3218,0)

    override var tile: io.rsbox.api.world.Tile = _tile

}