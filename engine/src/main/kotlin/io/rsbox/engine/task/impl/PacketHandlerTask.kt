package io.rsbox.engine.task.impl

import io.rsbox.engine.Engine
import io.rsbox.engine.service.impl.GameService
import io.rsbox.engine.task.GameTask

/**
 * @author Kyle Escobar
 */

class PacketHandlerTask : GameTask {
    override fun execute(service: GameService) {
        Engine.world.players.forEach { player ->
            player.context.handleMessages()
        }
    }
}