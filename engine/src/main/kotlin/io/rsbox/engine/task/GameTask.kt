package io.rsbox.engine.task

import io.rsbox.engine.service.impl.GameService

/**
 * @author Kyle Escobar
 */

interface GameTask {
    fun execute(service: GameService)
}