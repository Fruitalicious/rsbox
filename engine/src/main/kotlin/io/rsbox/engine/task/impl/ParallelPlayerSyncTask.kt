package io.rsbox.engine.task.impl

import io.rsbox.engine.Engine
import io.rsbox.engine.model.entity.LivingEntity
import io.rsbox.engine.service.impl.GameService
import io.rsbox.engine.task.GameTask
import io.rsbox.engine.task.sync.PlayerPostSyncTask
import io.rsbox.engine.task.sync.PlayerPreSyncTask
import io.rsbox.engine.task.sync.PlayerSyncTask
import io.rsbox.engine.task.sync.SyncTask
import mu.KLogging
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Phaser

/**
 * @author Kyle Escobar
 */

class ParallelPlayerSyncTask(private val executor: ExecutorService) : GameTask {

    private val phaser = Phaser(1)
    override fun execute(service: GameService) {
        val world = Engine.world
        val players = world.players
        val playerCount = world.players.count()

        phaser.bulkRegister(playerCount)
        players.forEach { player ->
            submit(phaser, executor, player, PlayerPreSyncTask)
        }
        phaser.arriveAndAwaitAdvance()

        phaser.bulkRegister(playerCount)
        players.forEach { player ->
            submit(phaser, executor, player, PlayerSyncTask)
        }
        phaser.arriveAndAwaitAdvance()

        phaser.bulkRegister(playerCount)
        players.forEach { player ->
            submit(phaser, executor, player, PlayerPostSyncTask)
        }
        phaser.arriveAndAwaitAdvance()
    }

    private fun <T : LivingEntity> submit(phaser: Phaser, executor: ExecutorService, player: T, task: SyncTask<T>) {
        executor.execute {
            try {
                task.run(player)
            } catch(e : Exception) {
                logger.error("Error with task ${this::class.java.simpleName} for $player.", e)
            } finally {
                phaser.arriveAndDeregister()
            }
        }
    }

    companion object : KLogging()
}