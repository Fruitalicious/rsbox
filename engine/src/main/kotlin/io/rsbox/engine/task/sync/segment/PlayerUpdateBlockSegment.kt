package io.rsbox.engine.task.sync.segment

import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.net.packet.model.DataOrder
import io.rsbox.engine.net.packet.model.DataTransformation
import io.rsbox.engine.net.packet.model.DataType
import io.rsbox.engine.net.packet.model.GamePacketBuilder
import io.rsbox.engine.task.sync.SyncSegment
import io.rsbox.engine.task.sync.block.UpdateBlockType

/**
 * @author Kyle Escobar
 */

class PlayerUpdateBlockSegment(val other: Player, private val newPlayer: Boolean) : SyncSegment {
    override fun encode(buf: GamePacketBuilder) {
        var mask = other.blockBuffer.blockValue()

        if(newPlayer) {
            mask = mask or UpdateBlockType.APPEARANCE.mask
        }

        buf.put(DataType.BYTE, mask and 0xFF)

        if(other.hasBlock(UpdateBlockType.APPEARANCE)) {
            write(buf, UpdateBlockType.APPEARANCE)
        }
    }

    private fun write(buf: GamePacketBuilder, blockType: UpdateBlockType) {
        when(blockType) {
            UpdateBlockType.APPEARANCE -> {
                val appBuf = GamePacketBuilder()
                appBuf.put(DataType.BYTE, 0)
                appBuf.put(DataType.BYTE, -1)
                appBuf.put(DataType.BYTE, -1)

                val translation = arrayOf(-1, -1, -1, -1, 2, -1, 3, 5, 0, 4, 6, 1)

                val DEFAULT_LOOKS = intArrayOf(9, 14, 109, 26, 33, 36, 42)

                val DEFAULT_COLORS = intArrayOf(0, 3, 2, 0, 0)

                val arms = 6
                val hair = 8
                val beard = 11

                for(i in 0 until 12) {
                    if(translation[i] == -1) {
                        appBuf.put(DataType.BYTE, 0)
                    } else {
                        appBuf.put(DataType.SHORT, 0x100 + DEFAULT_LOOKS[translation[i]])
                    }
                }

                for(i in 0 until 5) {
                    val color = Math.max(0, DEFAULT_COLORS[i])
                    appBuf.put(DataType.BYTE, color)
                }

                val animations = intArrayOf(808, 823, 819, 820, 821, 822, 824)

                animations.forEach { anim ->
                    appBuf.put(DataType.SHORT, anim)
                }

                appBuf.putString(other.username)
                appBuf.put(DataType.SHORT, 3)
                appBuf.put(DataType.SHORT, 0)
                appBuf.put(DataType.SHORT, 0)

                // LENGTH
                buf.put(type = DataType.BYTE, order = DataOrder.BIG, transformation = DataTransformation.SUBTRACT, value = appBuf.byteBuf.readableBytes())
                buf.putBytes(DataTransformation.ADD, appBuf.byteBuf)
            }
        }
    }
}