package io.rsbox.engine.model.entity

import io.rsbox.engine.model.world.Direction

/**
 * @author Kyle Escobar
 */

open class LivingEntity : Entity(), io.rsbox.api.entity.LivingEntity {
    override var index: Int = -1

    var lastFacingDirection: Direction = Direction.SOUTH
}