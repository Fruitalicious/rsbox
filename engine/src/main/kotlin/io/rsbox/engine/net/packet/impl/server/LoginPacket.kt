package io.rsbox.engine.net.packet.impl.server

import io.netty.buffer.Unpooled
import io.rsbox.engine.model.world.Chunk
import io.rsbox.engine.model.world.Tile
import io.rsbox.engine.net.packet.ServerPacket
import io.rsbox.engine.net.packet.model.*
import io.rsbox.engine.service.impl.XteaKeyService

/**
 * @author Kyle Escobar
 */

class LoginPacket(val playerIndex: Int, val tile: Tile, val playerTiles: IntArray, val xteaKeyService: XteaKeyService) : ServerPacket() {
    override var opcode = 0
    override var type = PacketType.VARIABLE_SHORT

    override fun encode() {
        initPacket()

        // GPI
        packet.putBytes(encodeGpi(playerIndex, tile, playerTiles))

        // Z
        packet.put(type = DataType.SHORT, order = DataOrder.LITTLE, transformation = DataTransformation.ADD, value = (tile.z shr 3))

        // X
        packet.put(type = DataType.SHORT, transformation = DataTransformation.ADD, value = (tile.x shr 3))

        // XTEAS
        packet.putBytes(encodeXteas(xteaKeyService, tile))
    }

    private fun encodeGpi(playerIndex: Int, tile: Tile, palyerTiles: IntArray): ByteArray {
        val buf = GamePacketBuilder()
        buf.switchToBitAccess()
        buf.putBits(30, tile.as30BitInteger)
        for(i in 1 until 2048) {
            if(i != playerIndex) {
                buf.putBits(18, playerTiles[i])
            }
        }
        buf.switchToByteAccess()
        val gpi = ByteArray(buf.byteBuf.readableBytes())
        buf.byteBuf.readBytes(gpi)

        return gpi
    }

    private fun encodeXteas(xteaKeyService: XteaKeyService, tile: Tile): ByteArray {
        val chunkX = tile.x shr 3
        val chunkZ = tile.z shr 3

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