package io.rsbox.api

import io.rsbox.api.cache.CacheData

/**
 * Represents our game engine's main class.
 *
 * @author Kyle Escobar
 */
interface Engine {

    val cache: CacheData
}