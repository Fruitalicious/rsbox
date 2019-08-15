package io.rsbox.server.sync.segment

import io.rsbox.server.net.packet.builder.GamePacketBuilder
import io.rsbox.server.sync.SyncSegment

/**
 * @author Kyle Escobar
 */

class RemoveLocalPlayerSegment(private val updateTileHash: Boolean) : SyncSegment {
    override fun encode(buf: GamePacketBuilder) {
        buf.putBits(1, 1)
        buf.putBits(1, 0)
        buf.putBits(2, 0)
        buf.putBits(1, if(updateTileHash) 1 else 0)
    }
}