package io.rsbox.server.task.parallel

import io.rsbox.server.model.entity.LivingEntity
import io.rsbox.server.model.world.World
import io.rsbox.server.service.impl.GameService
import io.rsbox.server.sync.SyncTask
import io.rsbox.server.sync.task.PlayerPostSyncTask
import io.rsbox.server.sync.task.PlayerPreSyncTask
import io.rsbox.server.sync.task.PlayerSyncTask
import io.rsbox.server.task.GameTask
import mu.KLogging
import java.util.concurrent.ExecutorService
import java.util.concurrent.Phaser

/**
 * @author Kyle Escobar
 */

class ParallelSyncTask(private val executor: ExecutorService) : GameTask {

    private val phaser = Phaser(1)

    override fun execute(world: World, service: GameService) {
        val players = world.players
        val playerCount = players.count()
        val npcs = world.npcs
        val rawNpcs = npcs.entityList
        val npcCount = npcs.count()

        /**
         * Player Pre-Sync
         */
        phaser.bulkRegister(playerCount)
        players.forEach { p ->
            submit(phaser, executor, p, PlayerPreSyncTask)
        }
        phaser.arriveAndAwaitAdvance()

        /**
         * Player Sync
         */
        phaser.bulkRegister(playerCount)
        players.forEach { p ->
            submit(phaser,executor, p, PlayerSyncTask)
        }
        phaser.arriveAndAwaitAdvance()

        /**
         * Player Post-Sync
         */
        phaser.bulkRegister(playerCount)
        players.forEach { p ->
            submit(phaser, executor, p, PlayerPostSyncTask)
        }
        phaser.arriveAndAwaitAdvance()
    }

    private fun <T: LivingEntity> submit(phaser: Phaser, executor: ExecutorService, entity: T, task: SyncTask<T>) {
        executor.execute {
            try {
                task.run(entity)
            } catch(e : Exception) {
                logger.error("Error with task ${this::class.java.simpleName} for $entity")
            } finally {
                phaser.arriveAndDeregister()
            }
        }
    }

    companion object : KLogging()
}