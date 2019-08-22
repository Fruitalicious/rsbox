package io.rsbox.engine.model.world

/**
 * @author Kyle Escobar
 */

class Tile : io.rsbox.api.world.Tile {
    private val coordinate: Int

    override val x: Int get() = coordinate and 0x7FFF

    override val z: Int get() = (coordinate shr 15) and 0x7FFF

    override val height: Int get() = coordinate ushr 30

    override val topLeftRegionX: Int get() = (x shr 3) - 6

    override val topLeftRegionZ: Int get() = (z shr 3) - 6

    override val regionId: Int get() = ((x shr 6) shl 8) or (z shr 6)

    override val as30BitInteger: Int get() = (z and 0x3FFF) or ((x and 0x3FFF) shl 14) or ((height and 0x3) shl 28)

    override val asTileHashMultiplier: Int get() = (z shr 13) or ((x shr 13) shl 8) or ((height and 0x3) shl 16)

    val chunkCoords: ChunkCoords get() = ChunkCoords.fromTile(this)

    private constructor(coordinate: Int) {
        this.coordinate = coordinate
    }

    constructor(x: Int, z: Int, height: Int = 0) : this((x and 0x7FFF) or ((z and 0x7FFF) shl 15) or (height shl 30))

    constructor(other: Tile) : this(other.x, other.z, other.height)

    fun isWithinRadius(other: Tile, radius: Int): Boolean = isWithinRadius(other.x, other.z, other.height, radius)

    fun isWithinRadius(otherX: Int, otherZ: Int, otherHeight: Int, radius: Int): Boolean {
        if(otherHeight != height) {
            return false
        }

        val dx = Math.abs(x - otherX)
        val dz = Math.abs(z - otherZ)
        return dx <= radius && dz <= radius
    }

    fun step(direction: Direction, num: Int = 1): Tile = Tile(this.x + (num * direction.getDeltaX()), this.z + (num * direction.getDeltaZ()), this.height)

    companion object {
        /**
         * The total amount of height levels that can be used in the game.
         */
        const val TOTAL_HEIGHT_LEVELS = 4

        fun fromRotatedHash(packed: Int): Tile {
            val x = ((packed shr 14) and 0x3FF) shl 3
            val z = ((packed shr 3) and 0x7FF) shl 3
            val height = (packed shr 28) and 0x3
            return Tile(x, z, height)
        }

        fun from30BitHash(packed: Int): Tile {
            val x = ((packed shr 14) and 0x3FFF)
            val z = ((packed) and 0x3FFF)
            val height = (packed shr 28)
            return Tile(x, z, height)
        }

        fun fromRegion(region: Int): Tile {
            val x = ((region shr 8) shl 6)
            val z = ((region and 0xFF) shl 6)
            return Tile(x, z)
        }
    }
}