package io.rsbox.api

import io.rsbox.api.world.World

/**
 * This is the base static object which hold references to our engine.
 * This can be called statically from anywhere.
 *
 * @author Kyle Escobar
 */
object RSBox {

    /**
     * Reference to our Server class.
     */
    lateinit var server: Server

    /**
     * Reference to our engine class.
     */
    lateinit var engine: Engine

    /**
     * Reference to our game engine world.
     */
    lateinit var world: World
}