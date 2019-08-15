package io.rsbox.api.world

/**
 * @author Kyle Escobar
 */

class Tile {
    private val coordinate: Int

    val x: Int get() = coordinate and 0x7FFF

    val z: Int get() = (coordinate shr 15) and 0x7FFF

    val height: Int get() = coordinate ushr 30

    val topLeftRegionX: Int get() = (x shr 3) - 6

    val topLeftRegionZ: Int get() = (z shr 3) - 6

    val regionId: Int get() = ((x shr 6) shl 8) or (z shr 6)

    val as30BitInteger: Int get() = (z and 0x3FFF) or ((x and 0x3FFF) shl 14) or ((height and 0x3) shl 28)

    val asTileHashMultiplier: Int get() = (z shr 13) or ((x shr 13) shl 8) or ((height and 0x3) shl 16)

    private constructor(coordinate: Int) {
        this.coordinate = coordinate
    }

    constructor(x: Int, z: Int, height: Int = 0) : this((x and 0x7FFF) or ((z and 0x7FFF) shl 15) or (height shl 30))

    constructor(other: Tile) : this(other.x, other.z, other.height)

    fun step(direction: Direction, num: Int = 1): Tile = Tile(this.x + (num * direction.getDeltaX()), this.z + (num * direction.getDeltaZ()), this.height)
}