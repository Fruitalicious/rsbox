package io.rsbox.api.world

/**
 * @author Kyle Escobar
 */

interface Tile {
    val x: Int

    val z: Int

    val height: Int

    val topLeftRegionX: Int

    val topLeftRegionZ: Int

    val regionId: Int

    val as30BitInteger: Int

    val asTileHashMultiplier: Int
}