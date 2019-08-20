package io.rsbox.engine.task.sync.segment

import io.rsbox.engine.net.packet.model.GamePacketBuilder
import io.rsbox.engine.task.sync.SyncSegment

/**
 * @author Kyle Escobar
 */

class SetByteAccessSegment : SyncSegment {
    override fun encode(buf: GamePacketBuilder) {
        buf.switchToByteAccess()
    }
}