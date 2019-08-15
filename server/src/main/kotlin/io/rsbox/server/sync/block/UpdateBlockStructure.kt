package io.rsbox.server.sync.block

import io.rsbox.server.net.packet.MessageValue

/**
 * @author Kyle Escobar
 */

data class UpdateBlockStructure(val bit: Int, val values: List<MessageValue>)