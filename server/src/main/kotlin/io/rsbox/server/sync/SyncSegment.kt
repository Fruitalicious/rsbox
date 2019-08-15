package io.rsbox.server.sync

import io.rsbox.server.net.packet.builder.GamePacketBuilder

/**
 * @author Kyle Escobar
 */

interface SyncSegment {
    fun encode(buf: GamePacketBuilder)
}