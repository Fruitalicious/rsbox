package io.rsbox.server.service.impl

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.rsbox.server.Launcher
import io.rsbox.server.model.world.World
import io.rsbox.server.net.packet.MessageEncoderSet
import io.rsbox.server.net.packet.MessageStructureSet
import io.rsbox.server.net.packet.MessageDecoderSet
import io.rsbox.server.service.Service
import io.rsbox.server.task.GameTask
import io.rsbox.server.task.MessageHandlerTask
import io.rsbox.server.task.parallel.ParallelSyncTask
import io.rsbox.server.task.sequential.SequentialSyncTask
import mu.KLogging
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * @author Kyle Escobar
 */

class GameService : Service() {

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
        ThreadFactoryBuilder()
            .setNameFormat("GAME-SERVICE")
            .setUncaughtExceptionHandler { t, e -> logger.error("Error with thread $t.", e)}
            .build()
    )

    internal val world: World = Launcher.server.world as World

    lateinit var messages: MessageStructureSet
    lateinit var encoders: MessageEncoderSet
    lateinit var decoders: MessageDecoderSet

    private val tasks = mutableListOf<GameTask>()

    override fun onStart() {
        messages = MessageStructureSet()
        encoders = MessageEncoderSet()
        decoders = MessageDecoderSet()

        messages.load(encoders, decoders)

        /**
         * Load tasks
         */
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        val sequentialTasks = availableProcessors == 1

        if(sequentialTasks) {
            tasks.addAll(arrayOf(
                MessageHandlerTask(),
                SequentialSyncTask()
            ))
            logger.info { "Server using sequential task mode. ${tasks.size} will be handled per game tick." }
        } else {
            val executor = Executors.newFixedThreadPool(availableProcessors,
                ThreadFactoryBuilder()
                    .setNameFormat("GAME-TICK")
                    .setUncaughtExceptionHandler { t, e -> logger.error("Error with thread $t.", e) }
                    .build())

            tasks.addAll(arrayOf(
                MessageHandlerTask(),
                ParallelSyncTask(executor)
            ))
            logger.info("Server using parallel task mode. ${tasks.size} will be handled per cycles on $availableProcessors threads.")
        }

        executor.scheduleAtFixedRate(this::cycle, 0, 600L, TimeUnit.MILLISECONDS)
        logger.info { "Game tick scheduler is now running every 600ms." }
    }

    override fun onStop() {

    }

    private fun cycle() {
        tasks.forEach { task ->
            try {
                task.execute(world, this)
            } catch(e : Exception) {
                logger.error("Error with task ${task.javaClass.simpleName}.", e)
            }
        }
    }

    companion object : KLogging()
}