package io.rsbox.server.sync.block.dsl

import io.rsbox.server.net.packet.MessageValue
import io.rsbox.server.sync.block.UpdateBlockType

/**
 * @author Kyle Escobar
 */

class Block(val block: UpdateBlockType) {
    var bit: Int = -1
    var structure = mutableListOf<MessageValue>()
}