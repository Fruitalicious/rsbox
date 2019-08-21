package io.rsbox.engine.task.sync.segment

import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.model.world.Tile
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

    private val blockOrder = arrayOf(
        UpdateBlockType.HITMARK,
        UpdateBlockType.GFX,
        UpdateBlockType.MOVEMENT,
        UpdateBlockType.FORCE_MOVEMENT,
        UpdateBlockType.FORCE_CHAT,
        UpdateBlockType.FACE_TILE,
        UpdateBlockType.APPEARANCE,
        UpdateBlockType.FACE_ENTITY,
        UpdateBlockType.ANIMATION
    )

    override fun encode(buf: GamePacketBuilder) {
        var mask = other.blockBuffer.blockValue()

        var forceFaceEntity = false
        var forceFaceTile = false
        var forceFace: Tile? = null

        if(newPlayer) {
            mask = mask or UpdateBlockType.APPEARANCE.mask

            when {
                other.blockBuffer.faceAngle != 0 -> {
                    mask = mask or UpdateBlockType.FACE_TILE.mask
                    forceFaceTile = true
                }

                other.blockBuffer.faceEntityIndex != -1 -> {
                    mask = mask or UpdateBlockType.FACE_ENTITY.mask
                    forceFaceEntity = true
                }

                else -> {
                    mask = mask or UpdateBlockType.FACE_TILE.mask
                    forceFace = other._tile.step(other.lastFacingDirection)
                }
            }
        }

        if(mask >= 0x100) {
            mask = mask or 0x8
            buf.put(DataType.BYTE, mask and 0xFF)
            buf.put(DataType.BYTE, mask shr 8)
        } else {
            buf.put(DataType.BYTE, mask and 0xFF)
        }

        blockOrder.forEach { blockType ->
            val force = when(blockType) {
                UpdateBlockType.FACE_TILE -> forceFaceTile || forceFace != null
                UpdateBlockType.FACE_ENTITY -> forceFaceEntity
                UpdateBlockType.APPEARANCE -> newPlayer
                else -> false
            }

            if(other.hasBlock(blockType) || force) {
                writeBlock(buf, blockType, forceFace)
            }
        }
    }

    private fun writeBlock(buf: GamePacketBuilder, blockType: UpdateBlockType, forceFace: Tile?) {
        when(blockType) {

            // TODO
            UpdateBlockType.HITMARK -> {
                buf.put(type = DataType.BYTE, order = DataOrder.BIG, transformation = DataTransformation.NEGATE, value = 0)
                buf.put(type = DataType.BYTE, order = DataOrder.BIG, transformation = DataTransformation.ADD, value = 0)
            }

            UpdateBlockType.GFX -> {
                buf.put(type = DataType.SHORT, order = DataOrder.LITTLE, transformation = DataTransformation.NONE, value = other.blockBuffer.graphicId)
                buf.put(type = DataType.INT, order = DataOrder.MIDDLE, transformation = DataTransformation.NONE, value = (other.blockBuffer.graphicHeight shl 16) or other.blockBuffer.graphicDelay)
            }

            UpdateBlockType.MOVEMENT -> {
                buf.put(type = DataType.BYTE, order = DataOrder.BIG, transformation = DataTransformation.ADD, value =
                if(other.blockBuffer.teleport) 127 else 1)
            }

            // TODO
            UpdateBlockType.FORCE_MOVEMENT -> {

            }

            // TODO
            UpdateBlockType.FORCE_CHAT -> {

            }

            UpdateBlockType.FACE_TILE -> {
                if(forceFace != null) {
                    val srcX = other._tile.x * 64
                    val srcZ = other._tile.z * 64
                    val dstX = forceFace.x * 64
                    val dstZ = forceFace.z * 64
                    val degreesX = (srcX - dstX).toDouble()
                    val degreesZ = (srcZ - dstZ).toDouble()
                    buf.put(type = DataType.SHORT, order = DataOrder.LITTLE, transformation = DataTransformation.NONE, value = (Math.atan2(degreesX, degreesZ) * 325.949).toInt() and 0x7ff)
                } else {
                    buf.put(type = DataType.SHORT, order = DataOrder.LITTLE, transformation = DataTransformation.NONE, value = other.blockBuffer.faceAngle)
                }
            }

            UpdateBlockType.APPEARANCE -> {
                val appBuf = GamePacketBuilder()
                appBuf.put(DataType.BYTE, 0)
                appBuf.put(DataType.BYTE, -1)
                appBuf.put(DataType.BYTE, -1)

                val translation = arrayOf(-1, -1, -1, -1, 2, -1, 3, 5, 0, 4, 6, 1)
                val default_looks = intArrayOf(0, 10, 18, 26, 33, 36, 42)
                val default_colors = intArrayOf(0, 0, 0, 0, 0)
                val animations = intArrayOf(0x328, 0x337, 0x333, 0x334, 0x335, 0x336, 0x338)

                for(i in 0 until 12) {
                    if(translation[i] == -1) {
                        appBuf.put(DataType.BYTE, 0)
                    } else {
                        appBuf.put(DataType.SHORT, 0x100 + default_looks[translation[i]])
                    }
                }

                for(i in 0 until 5) {
                    val color = Math.max(0, default_colors[i])
                    appBuf.put(DataType.BYTE, color)
                }

                animations.forEach { anim ->
                    appBuf.put(DataType.SHORT, anim)
                }

                appBuf.putString(other.username)
                appBuf.put(DataType.BYTE, 3)
                appBuf.put(DataType.SHORT, 0)
                appBuf.put(DataType.BYTE, 0)

                buf.put(type = DataType.BYTE, order = DataOrder.BIG, transformation = DataTransformation.SUBTRACT, value = appBuf.byteBuf.readableBytes())
                buf.putBytes(DataTransformation.ADD, appBuf.byteBuf)
            }

        }
    }
}