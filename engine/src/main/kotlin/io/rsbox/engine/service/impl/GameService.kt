package io.rsbox.engine.service.impl

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.rsbox.engine.net.packet.PacketSet
import io.rsbox.engine.service.Service
import io.rsbox.engine.task.GameTask
import io.rsbox.engine.task.impl.PacketHandlerTask
import io.rsbox.engine.task.impl.ParallelPlayerSyncTask
import mu.KLogging
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GameService : Service() {

    val packets = PacketSet()

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
        ThreadFactoryBuilder()
            .setNameFormat("game-thread")
            .setUncaughtExceptionHandler { t, e -> logger.error("Error with thread $t.", e) }
            .build())

    private val tasks = mutableListOf<GameTask>()

    override fun start() {
        packets.loadPackets()
        this.loadTasks()

        executor.scheduleAtFixedRate(this::cycle, 0, 600L, TimeUnit.MILLISECONDS)
    }

    override fun stop() {

    }

    private fun loadTasks() {
        val taskThreads = Runtime.getRuntime().availableProcessors()
        val taskExecutor = Executors.newFixedThreadPool(taskThreads,
            ThreadFactoryBuilder()
                .setNameFormat("game-task")
                .setUncaughtExceptionHandler { t, e -> logger.error("Error in thread $t.", e) }
                .build())

        tasks.addAll(arrayOf(
            PacketHandlerTask(),
            ParallelPlayerSyncTask(taskExecutor)
        ))
    }

    private fun cycle() {
        tasks.forEach { task ->
            try {
                task.execute(this)
            } catch(e : Exception) {
                logger.error("Error with task ${task.javaClass.simpleName}.", e)
            }
        }
    }

    companion object : KLogging()
}