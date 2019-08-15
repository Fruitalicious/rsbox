package io.rsbox.server.sync.segment

import io.rsbox.server.net.packet.builder.GamePacketBuilder
import io.rsbox.server.sync.SyncSegment

/**
 * @author Kyle Escobar
 */

class SetBitAccessSegment : SyncSegment {
    override fun encode(buf: GamePacketBuilder) {
        buf.switchToBitAccess()
    }
}