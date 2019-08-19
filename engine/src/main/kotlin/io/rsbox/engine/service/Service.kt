package io.rsbox.engine.service

/**
 * @author Kyle Escobar
 */

abstract class Service {
    var loaded: Boolean = false

    abstract fun start()

    abstract fun stop()
}