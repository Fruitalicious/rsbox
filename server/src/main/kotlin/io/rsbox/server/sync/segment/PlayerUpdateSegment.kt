package io.rsbox.server.sync.segment

import io.rsbox.api.world.Tile
import io.rsbox.server.model.entity.Player
import io.rsbox.server.model.world.World
import io.rsbox.server.net.packet.builder.DataType
import io.rsbox.server.net.packet.builder.GamePacketBuilder
import io.rsbox.server.sync.SyncSegment
import io.rsbox.server.sync.block.UpdateBlockType
import java.lang.RuntimeException

/**
 * @author Kyle Escobar
 */

class PlayerUpdateSegment(val other: Player, private val newPlayer: Boolean) : SyncSegment {
    override fun encode(buf: GamePacketBuilder) {
        var mask = other.blockBuffer.blockValue()
        val blocks = (other.world as World).playerUpdateBlocks

        var forceFaceEntity = false
        var forceFaceTile = false

        var forceFace: Tile? = null

        if(newPlayer) {
            mask = mask or blocks.updateBlocks[UpdateBlockType.APPEARANCE]!!.bit

            when {
                other.blockBuffer.faceDegrees != 0 -> {
                    mask = mask or blocks.updateBlocks[UpdateBlockType.FACE_TILE]!!.bit
                    forceFaceTile = true
                }
                other.blockBuffer.faceEntityIndex != -1 -> {
                    mask = mask or blocks.updateBlocks[UpdateBlockType.FACE_ENTITY]!!.bit
                    forceFaceEntity = true
                }
                else -> {
                    mask = mask or blocks.updateBlocks[UpdateBlockType.FACE_TILE]!!.bit
                    forceFace = other.tile.step(other.direction)
                }
            }
        }

        if(mask >= 0x100) {
            mask = mask or blocks.updateBlockMask
            buf.put(DataType.BYTE, mask and 0xFF)
            buf.put(DataType.BYTE, mask shr 8)
        } else {
            buf.put(DataType.BYTE, mask and 0xFF)
        }

        blocks.updateBlockOrder.forEach { type ->
            val force = when(type) {
                UpdateBlockType.FACE_TILE -> forceFaceTile || forceFace != null
                UpdateBlockType.FACE_ENTITY -> forceFaceEntity
                UpdateBlockType.APPEARANCE -> newPlayer
                else -> false
            }

            if(other.hasBlock(type) || force) {
                sendBlock(buf, type, forceFace)
            }
        }
    }

    private fun sendBlock(buf: GamePacketBuilder, type: UpdateBlockType, forceFace: Tile?) {
        val blocks = (other.world as World).playerUpdateBlocks

        when(type) {
            UpdateBlockType.APPEARANCE -> {
                val abuf = GamePacketBuilder()
                abuf.put(DataType.BYTE, other.appearance.gender.id)
                abuf.put(DataType.BYTE, -1) // Skull icon
                abuf.put(DataType.BYTE, -1) // Overhead prayer icon

                val disguise = other.disguisedEntityId >= 0

                if(!disguise) {
                    val trans = arrayOf(-1, -1, -1, -1, 2, -1, 3, 5, 0, 4, 6, 1)
                    val arms = 6
                    val hair = 8
                    val beard = 11

                    for(i in 0 until 12) {
                        if(trans[i] == -1) {
                            abuf.put(DataType.BYTE, 0)
                        } else {
                            abuf.put(DataType.SHORT, 0x100 + other.appearance.looks[trans[i]])
                        }
                    }
                } else {
                    abuf.put(DataType.SHORT, 0xFFFF)
                    abuf.put(DataType.SHORT, other.disguisedEntityId)
                }

                for(i in 0 until 5) {
                    val color = Math.max(0, other.appearance.colors[i])
                    abuf.put(DataType.BYTE, color)
                }

                if(!disguise) {
                    val animations = intArrayOf(808, 823, 819, 820, 821, 822, 824)

                    animations.forEach { anim ->
                        abuf.put(DataType.SHORT, anim)
                    }
                } else {
                    // TODO pull anims for disguised enitity from cache
                }

                abuf.putString(other.displayName)
                abuf.put(DataType.BYTE, 3) // Combat level
                abuf.put(DataType.SHORT, 0)
                abuf.put(DataType.BYTE, 0)

                val structure = blocks.updateBlocks[type]!!.values
                buf.put(structure[0].type, structure[0].order, structure[0].trans, abuf.byteBuf.readableBytes())
                buf.putBytes(structure[1].trans, abuf.byteBuf)
            }

            else -> throw RuntimeException("Unhandled update block type: $type.")
        }
    }
}