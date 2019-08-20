package io.rsbox.engine.task.sync

import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.model.world.Chunk
import io.rsbox.engine.model.world.Coordinate
import io.rsbox.engine.model.world.Tile
import io.rsbox.engine.net.packet.impl.server.RebuildRegionPacket

/**
 * @author Kyle Escobar
 */

object PlayerPreSyncTask : SyncTask<Player> {
    override fun run(entity: Player) {
        val player = entity

        val last = player.lastKnownRegionBase
        val current = player._tile

        if(last == null || shouldRebuildRegion(last, current)) {
            val regionX = ((current.x shr 3) - (Chunk.MAX_VIEWPORT shr 4)) shl 3
            val regionZ = ((current.z shr 3) - (Chunk.MAX_VIEWPORT shr 4)) shl 3

            player.lastKnownRegionBase = Coordinate(regionX, regionZ, current.height)

            val xteaKeyService = player._world.xteaKeyService
            player.sendPacket(RebuildRegionPacket(current.x shr 3, current.z shr 3, xteaKeyService))
        }
    }

    private fun shouldRebuildRegion(old: Coordinate, new: Tile): Boolean {
        val dx = new.x - old.x
        val dz = new.z - old.z

        return dx <= Player.NORMAL_VIEW_DISTANCE || dx >= Chunk.MAX_VIEWPORT - Player.NORMAL_VIEW_DISTANCE - 1
                || dz <= Player.NORMAL_VIEW_DISTANCE || dz >= Chunk.MAX_VIEWPORT - Player.NORMAL_VIEW_DISTANCE - 1
    }
}