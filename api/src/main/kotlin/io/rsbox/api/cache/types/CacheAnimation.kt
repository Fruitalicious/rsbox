package io.rsbox.api.cache.types

import io.rsbox.api.cache.CacheDef

/**
 * The animations from the game cache.
 *
 * @author Kyle Escobar
 */
interface CacheAnimation : CacheDef {

    /**
     * The number of game ticks this animation takes to complete.
     */
    val duration: Int

}