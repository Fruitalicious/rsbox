package io.rsbox.engine.task.sync.segment

import io.rsbox.engine.net.packet.model.GamePacketBuilder
import io.rsbox.engine.task.sync.SyncSegment

/**
 * @author Kyle Escobar
 */

class SignalPlayerUpdateBlockSegment : SyncSegment {
    override fun encode(buf: GamePacketBuilder) {
        buf.putBits(1, 1)
        buf.putBits(1, 1)
        buf.putBits(2, 0)
    }
}