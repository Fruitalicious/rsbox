package io.rsbox.api.entity

import io.rsbox.api.Server
import io.rsbox.api.world.World

/**
 * @author Kyle Escobar
 */

interface Entity {
    val world: World

    val server: Server
}