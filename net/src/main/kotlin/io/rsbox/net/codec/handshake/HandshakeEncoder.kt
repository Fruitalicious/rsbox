package io.rsbox.net.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.rsbox.engine.ServerResponse

/**
 * @author Kyle Escobar
 */

class HandshakeEncoder : MessageToByteEncoder<ServerResponse>() {
    override fun encode(ctx: ChannelHandlerContext, msg: ServerResponse, out: ByteBuf) {
        out.writeByte(msg.id)
    }
}