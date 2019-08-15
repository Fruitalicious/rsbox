package io.rsbox.server.task.sequential

import io.rsbox.server.model.world.World
import io.rsbox.server.service.impl.GameService
import io.rsbox.server.sync.task.PlayerPostSyncTask
import io.rsbox.server.sync.task.PlayerPreSyncTask
import io.rsbox.server.sync.task.PlayerSyncTask
import io.rsbox.server.task.GameTask

/**
 * @author Kyle Escobar
 */

class SequentialSyncTask : GameTask {

    override fun execute(world: World, service: GameService) {
        val players = world.players
        val npcs = world.npcs
        val rawNpcs = world.npcs.entityList

        /**
         * Player Pre-Sync
         */
        players.forEach { p ->
            PlayerPreSyncTask.run(p)
        }

        /**
         * Player Sync
         */
        players.forEach { p ->
            PlayerSyncTask.run(p)
        }

        /**
         * Player Post-Sync
         */
        players.forEach { p ->
            PlayerPostSyncTask.run(p)
        }
    }

}