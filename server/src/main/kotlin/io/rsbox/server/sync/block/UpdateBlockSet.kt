package io.rsbox.server.sync.block

import io.rsbox.server.sync.block.dsl.UpdateBlock
import mu.KLogging
import org.reflections.Reflections
import java.util.*

/**
 * @author Kyle Escobar
 */

class UpdateBlockSet {
    var updateOpcode = -1

    var bigUpdateOpcode = -1

    var updateBlockMask = -1

    val updateBlockOrder = mutableListOf<UpdateBlockType>()
    val updateBlocks = EnumMap<UpdateBlockType, UpdateBlockStructure>(UpdateBlockType::class.java)

    fun load(updateBlock: UpdateBlock) {
        check(this.updateOpcode == -1)
        check(this.updateBlockMask == -1)
        check(this.updateBlockOrder.isEmpty())
        check(this.updateBlocks.isEmpty())

        updateOpcode = updateBlock.opcode
        bigUpdateOpcode = updateBlock.bigopcode
        updateBlockMask = updateBlock.mask

        updateBlock.order.forEach { o ->
            updateBlockOrder.add(o)
        }

        updateBlock.blocks.forEach { packet ->
            val blockType = packet.block
            val bit = packet.bit
            val structure = packet.structure
            updateBlocks[blockType] = UpdateBlockStructure(bit = bit, values = structure)
        }
    }

    fun loadAll() {
        logger.info { "Loading segment blocks." }
        val reflections = Reflections("io.rsbox.server")
        val classes = reflections.getSubTypesOf(UpdateBlock::class.java)
        classes.forEach { clazz ->
            val inst = clazz.newInstance()
            this.load(inst)
        }

        logger.info { "Loaded ${classes.size} segment blocks."}
    }

    companion object : KLogging()
}