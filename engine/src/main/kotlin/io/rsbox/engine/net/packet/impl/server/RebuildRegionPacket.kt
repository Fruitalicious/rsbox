package io.rsbox.engine.net.packet.impl.server

import io.netty.buffer.Unpooled
import io.rsbox.engine.model.world.Chunk
import io.rsbox.engine.model.world.Tile
import io.rsbox.engine.net.packet.ServerPacket
import io.rsbox.engine.net.packet.model.DataOrder
import io.rsbox.engine.net.packet.model.DataTransformation
import io.rsbox.engine.net.packet.model.DataType
import io.rsbox.engine.net.packet.model.PacketType
import io.rsbox.engine.service.impl.XteaKeyService

/**
 * @author Kyle Escobar
 */

class RebuildRegionPacket(val x: Int, val z: Int, val xteaKeyService: XteaKeyService) : ServerPacket() {
    override var opcode = 0
    override var type = PacketType.VARIABLE_SHORT

    override fun encode() {
        initPacket()

        // Z
        packet.put(type = DataType.SHORT, order = DataOrder.LITTLE, transformation = DataTransformation.ADD, value = z)

        // X
        packet.put(type = DataType.SHORT, transformation = DataTransformation.ADD, value = x)

        // XTEAS
        packet.putBytes(encodeXteas(xteaKeyService, x, z))
    }

    private fun encodeXteas(xteaKeyService: XteaKeyService, x: Int, z: Int): ByteArray {
        val chunkX = x shr 3
        val chunkZ = z shr 3

        val lx = (chunkX - (Chunk.MAX_VIEWPORT shr 4)) shr 3
        val rx = (chunkX + (Chunk.MAX_VIEWPORT shr 4)) shr 3
        val lz = (chunkZ - (Chunk.MAX_VIEWPORT shr 4)) shr 3
        val rz = (chunkZ + (Chunk.MAX_VIEWPORT shr 4)) shr 3

        val buf = Unpooled.buffer(Short.SIZE_BYTES + (Int.SIZE_BYTES * 10))
        var forceSend = false
        if((chunkX / 8 == 48 || chunkX / 8 == 49) && chunkZ / 8 == 48) {
            forceSend = true
        }
        if(chunkX / 8 == 48 && chunkZ / 8 == 148) {
            forceSend = true
        }

        var count = 0
        buf.writeShort(count)
        for(x in lx..rx) {
            for(z in lz..rz) {
                if(!forceSend || z != 49 && z != 149 && z != 147 && x != 50 && (x != 49 || z != 47)) {
                    val region = z + (x shl 8)
                    val keys = xteaKeyService.get(region)
                    for(xteaKey in keys) {
                        buf.writeInt(xteaKey)
                    }
                    count++
                }
            }
        }
        buf.setShort(0, count)

        val xteas = ByteArray(buf.readableBytes())
        buf.readBytes(xteas)
        return xteas
    }
}