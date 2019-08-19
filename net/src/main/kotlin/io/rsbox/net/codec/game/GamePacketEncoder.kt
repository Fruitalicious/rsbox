package io.rsbox.net.codec.game

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.rsbox.engine.net.packet.model.GamePacket
import io.rsbox.engine.net.packet.model.PacketType
import io.rsbox.util.IsaacRandom
import mu.KLogging
import java.text.DecimalFormat

/**
 * @author Kyle Escobar
 */

class GamePacketEncoder(private val random: IsaacRandom) : MessageToByteEncoder<GamePacket>() {
    override fun encode(ctx: ChannelHandlerContext, msg: GamePacket, out: ByteBuf) {
        if(msg.type == PacketType.VARIABLE_BYTE && msg.length >= 256) {
            logger.error("Packet length {} for type 'variable-byte' is too long.", DecimalFormat().format(msg.length))
            return
        }

        if(msg.type == PacketType.VARIABLE_SHORT && msg.length >= 65536) {
            logger.error("Packet length {} for type 'variable-short' is too long.", DecimalFormat().format(msg.length))
            return
        }

        out.writeByte((msg.opcode + (random.nextInt())) and 0xFF)
        when(msg.type) {
            PacketType.VARIABLE_BYTE -> out.writeByte(msg.length)
            PacketType.VARIABLE_SHORT -> out.writeShort(msg.length)
            else -> {}
        }
        out.writeBytes(msg.payload)
        msg.payload.release()
    }

    companion object : KLogging()
}