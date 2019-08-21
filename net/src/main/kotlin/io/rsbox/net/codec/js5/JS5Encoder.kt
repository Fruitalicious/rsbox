package io.rsbox.net.codec.js5

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
 * @author Kyle Escobar
 */

class JS5Encoder : MessageToByteEncoder<JS5Response>() {
    override fun encode(ctx: ChannelHandlerContext, msg: JS5Response, out: ByteBuf) {
        out.writeByte(msg.index)
        out.writeShort(msg.archive)

        msg.data.forEach { data ->
            if(out.writerIndex() % 512 == 0) {
                out.writeByte(-1)
            }
            out.writeByte(data.toInt())
        }
    }
}