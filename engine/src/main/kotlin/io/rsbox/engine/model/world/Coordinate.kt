package io.rsbox.engine.model.world

/**
 * @author Kyle Escobar
 */

data class Coordinate(val x: Int, val z: Int, val height: Int) {
    fun toLocal(other: Tile): Tile = Tile(((other.x shr 3) - (x shr 3)) shl 3, ((other.z shr 3) - (z shr 3)) shl 3, height)

    val tile: Tile get() = Tile(x, z, height)
}