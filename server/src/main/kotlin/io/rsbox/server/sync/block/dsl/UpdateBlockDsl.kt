package io.rsbox.server.sync.block.dsl

import io.rsbox.server.net.packet.MessageValue
import io.rsbox.server.net.packet.builder.DataOrder
import io.rsbox.server.net.packet.builder.DataSignature
import io.rsbox.server.net.packet.builder.DataTransformation
import io.rsbox.server.net.packet.builder.DataType
import io.rsbox.server.sync.block.UpdateBlockType

/**
 * @author Kyle Escobar
 */

fun UpdateBlock.builder(inst: UpdateBlock, init: UpdateBlockDsl.Builder.() -> Unit) {
    val builder = UpdateBlockDsl.Builder()
    init(builder)

    inst.order = builder.order
    inst.blocks = builder.blocks
}

object UpdateBlockDsl {
    @DslMarker
    annotation class UpdateBlockDslMarker

    @UpdateBlockDslMarker
    class Builder {
        lateinit var order: MutableList<UpdateBlockType>
        lateinit var blocks: MutableList<Block>

        fun build(): UpdateBlockReturn = UpdateBlockReturn(order, blocks)

        fun order(init: OrderBuilder.() -> Unit) {
            val builder = OrderBuilder()
            init(builder)

            builder.entry.forEach { index, value ->
                order.add(index, value)
            }
        }

        fun blocks(init: BlocksBuilder.() -> Unit) {
            val builder = BlocksBuilder()
            init(builder)

            val b = Block(builder.block)
            b.bit = builder.bit
            b.structure = builder.structure
            blocks.add(b)
        }
    }

    @UpdateBlockDslMarker
    class OrderBuilder {
        val entry = hashMapOf<Int, UpdateBlockType>()
    }

    @UpdateBlockDslMarker
    class BlocksBuilder {
        var block: UpdateBlockType = UpdateBlockType.APPEARANCE
        var bit: Int = -1
        val structure = mutableListOf<MessageValue>()

        fun frame(name: String, type: DataType, order: DataOrder = DataOrder.BIG, trans: DataTransformation = DataTransformation.NONE) {
            structure.add(MessageValue(
                id = name,
                type = type,
                order = order,
                trans = trans,
                sign = DataSignature.SIGNED
            ))
        }
    }
}

data class UpdateBlockReturn(val order: MutableList<UpdateBlockType>, val blocks: MutableList<Block>)