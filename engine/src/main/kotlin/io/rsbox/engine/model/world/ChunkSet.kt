package io.rsbox.engine.model.world

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

/**
 * @author Kyle Escobar
 */

class ChunkSet(val world: World) {

    fun copyChunksWithinRadius(chunkCoords: ChunkCoords, height: Int, radius: Int): ChunkSet {
        val new = ChunkSet(world)
        val surrounding = chunkCoords.getSurroundingCoords(radius)

        surrounding.forEach { coords ->
            val chunk = get(coords, createIfNeeded = true)!!
            val copy = Chunk(coords, chunk.heights)

            // TODO Setup collision matrix

            new.chunks[coords] = copy
        }
        return new
    }

    private val chunks = Object2ObjectOpenHashMap<ChunkCoords, Chunk>()

    internal val activeRegions = IntOpenHashSet()

    fun get(tile: Tile, createIfNeeded: Boolean = false): Chunk? = get(tile.chunkCoords, createIfNeeded)

    fun get(coords: ChunkCoords, createIfNeeded: Boolean = false): Chunk? {
        val chunk = chunks[coords]
        if(chunk != null) {
            return chunk
        } else if(!createIfNeeded) {
            return null
        }

        val regionId = coords.toTile().regionId
        val newChunk = Chunk(coords, Tile.TOTAL_HEIGHT_LEVELS)
        chunks[coords] = newChunk
        if(activeRegions.add(regionId)) {
            world.cache.createRegion(world, regionId)
        }
        return newChunk
    }

    fun getActiveChunkCount(): Int = chunks.size

    fun getActiveRegionCount(): Int = activeRegions.size

    fun getOrCreate(tile: Tile): Chunk = get(tile.chunkCoords, createIfNeeded = true)!!

    fun remove(coords: ChunkCoords): Boolean = chunks.remove(coords) != null
}