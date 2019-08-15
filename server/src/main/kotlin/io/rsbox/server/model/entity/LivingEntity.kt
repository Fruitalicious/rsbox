package io.rsbox.server.model.entity

import io.rsbox.api.world.Direction
import io.rsbox.api.world.Tile
import io.rsbox.server.sync.block.UpdateBlockBuf

/**
 * @author Kyle Escobar
 */

open class LivingEntity : Entity(), io.rsbox.api.entity.LivingEntity {
    var index: Int = -1

    var blockBuffer = UpdateBlockBuf()

    var lastTile: Tile? = null

    var lastChunkTile: Tile? = null

    var moved = false

    var direction: Direction = Direction.SOUTH

    var disguisedEntityId: Int = -1
}