package io.rsbox.net.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.rsbox.net.codec.js5.JS5Decoder
import io.rsbox.net.codec.js5.JS5Encoder
import io.rsbox.net.codec.login.LoginDecoder
import io.rsbox.net.codec.login.LoginEncoder
import mu.KLogging

/**
 * @author Kyle Escobar
 */

class HandshakeDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if(!buf.isReadable) return

        val opcode = buf.readByte().toInt()

        when(HandshakeType.values.firstOrNull { it.opcode == opcode }) {
            HandshakeType.JS5 -> {
                val p = ctx.channel().pipeline()
                p.addFirst("js5_encoder", JS5Encoder())
                p.addAfter("handshake_decoder", "js5_decoder", JS5Decoder())
            }

            HandshakeType.LOGIN -> {
                val p = ctx.channel().pipeline()
                val seed = (Math.random() * Long.MAX_VALUE).toLong()

                p.addFirst("login_encoder", LoginEncoder())
                p.addAfter("handshake_decoder", "login_decoder", LoginDecoder(seed))

                ctx.writeAndFlush(ctx.alloc().buffer(1).writeByte(0))
                ctx.writeAndFlush(ctx.alloc().buffer(8).writeLong(seed))
            }

            else -> {
                buf.readBytes(buf.readableBytes())
                logger.warn("Unhandled handshake opcode={} channel={}", opcode, ctx.channel())
                return
            }
        }

        /**
         * We can remove this decoder from the pipeline since the handshake has been established.
         * A new connection is made when the player sends a login request.
         */
        ctx.pipeline().remove(this)
        out.add(HandshakeMessage(opcode))
    }

    companion object : KLogging()
}