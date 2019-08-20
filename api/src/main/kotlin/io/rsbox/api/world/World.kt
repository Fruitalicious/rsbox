package io.rsbox.api.world

/**
 * Represents the game engine's world object.
 *
 * @author Kyle Escobar
 */
interface World {
    /**
     * Executed right after the world is initialized.
     */
    fun preLoad()

    /**
     * Executed to load world.
     */
    fun load()

    /**
     * Executed after everything else in the server has been initialized.
     */
    fun postLoad()
}