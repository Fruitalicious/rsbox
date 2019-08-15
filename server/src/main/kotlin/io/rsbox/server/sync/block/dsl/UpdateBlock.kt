package io.rsbox.server.sync.block.dsl

import io.rsbox.server.sync.block.UpdateBlockType

/**
 * @author Kyle Escobar
 */

abstract class UpdateBlock(val type: UpdateType, val opcode: Int, val bigopcode: Int = -1, val mask: Int = -1) {
    var order = mutableListOf<UpdateBlockType>()
    var blocks = mutableListOf<Block>()

    abstract fun build()
}