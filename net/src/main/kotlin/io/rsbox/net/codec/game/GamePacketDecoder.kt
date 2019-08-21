package io.rsbox.net.codec.game

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.rsbox.engine.net.packet.model.GamePacket
import io.rsbox.engine.net.packet.model.PacketType
import io.rsbox.engine.service.ServiceManager
import io.rsbox.engine.service.impl.GameService
import io.rsbox.net.codec.StatefulMessageDecoder
import io.rsbox.util.IsaacRandom
import mu.KLogging

/**
 * @author Kyle Escobar
 */

class GamePacketDecoder(private val random: IsaacRandom) : StatefulMessageDecoder<GamePacketDecoderState>(GamePacketDecoderState.OPCODE) {

    private var opcode = -1
    private var length = 0
    private var type = PacketType.FIXED

    private var ignore = false

    private val packets = ServiceManager[GameService::class.java]!!.packets

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>, state: GamePacketDecoderState) {
        when(state) {
            GamePacketDecoderState.OPCODE -> decodeOpcode(ctx, buf)
            GamePacketDecoderState.LENGTH -> decodeLength(buf)
            GamePacketDecoderState.PAYLOAD -> decodePayload(buf, out)
        }
    }

    private fun decodeOpcode(ctx: ChannelHandlerContext, buf: ByteBuf) {
        if(buf.isReadable) {
            opcode = (buf.readByte().toInt() - random.nextInt()) and 0xFF
            // Try to get packet class from opcode
            if (packets.getClientPacket(opcode) == null) {
                ignore = true

                if(ClientPacketTypes.forOpcode(opcode)!!.length == -1 || ClientPacketTypes.forOpcode(opcode)!!.length == -2) {
                    setState(GamePacketDecoderState.LENGTH)
                } else {
                    logger.warn("Client sent packet with non-configured opcode {}.", opcode)
                    val length = ClientPacketTypes.forOpcode(opcode)!!.length
                    buf.skipBytes(length)
                    return
                }
            }

            if(!ignore) {
                val gamepacket = packets.getClientPacket(opcode)!!
                type = gamepacket.type

                when (type) {
                    PacketType.FIXED -> {
                        val len = gamepacket.packet.length
                        if (length == -1) {
                            length = buf.readUnsignedByte().toInt()
                        } else if (len == -2) {
                            length = buf.readUnsignedShort()
                        }
                        buf.skipBytes(length)
                        setState(GamePacketDecoderState.OPCODE)
                    }

                    PacketType.VARIABLE_BYTE, PacketType.VARIABLE_SHORT -> {
                        setState(GamePacketDecoderState.LENGTH)
                    }

                    else -> {
                        throw IllegalStateException("Unhandled packet type $type for opcode $opcode.")
                    }
                }
            }
        }
    }

    private fun decodeLength(buf: ByteBuf) {
        if(buf.isReadable) {
            length = if(type == PacketType.VARIABLE_SHORT) buf.readUnsignedShort() else buf.readUnsignedByte().toInt()
            if(ignore) {
                logger.warn("Client sent packet with non-configured opcode {}.", opcode)
                buf.skipBytes(buf.readableBytes())
                return
            } else if(length != 0) {
                setState(GamePacketDecoderState.PAYLOAD)
            }
        }
    }

    private fun decodePayload(buf: ByteBuf, out: MutableList<Any>) {
        if(buf.readableBytes() >= length) {
            val payload = buf.readBytes(length)
            setState(GamePacketDecoderState.OPCODE)
            out.add(GamePacket(opcode, type, payload))
        }
    }

    companion object : KLogging()
}