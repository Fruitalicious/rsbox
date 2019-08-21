package io.rsbox.engine.task.sync

import io.rsbox.engine.net.packet.model.GamePacketBuilder

/**
 * @author Kyle Escobar
 */

interface SyncSegment {

    fun encode(buf: GamePacketBuilder)

}