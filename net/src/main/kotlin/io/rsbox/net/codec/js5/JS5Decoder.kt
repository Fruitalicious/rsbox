package io.rsbox.net.codec.js5

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.rsbox.engine.config.ServerConfig
import io.rsbox.engine.config.spec.ServerSpec
import io.rsbox.engine.ServerResponse
import io.rsbox.net.codec.StatefulMessageDecoder
import mu.KLogging

/**
 * @author Kyle Escobar
 */

class JS5Decoder : StatefulMessageDecoder<JS5DecoderState>(JS5DecoderState.JS5_HEADER) {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>, state: JS5DecoderState) {
        when(state) {
            JS5DecoderState.JS5_HEADER -> decodeHeader(ctx, buf)
            JS5DecoderState.JS5_ARCHIVE -> decodeArchive(buf, out)
        }
    }

    private fun decodeHeader(ctx: ChannelHandlerContext, buf: ByteBuf) {
        if(buf.readableBytes() >= 4) {
            val revision = buf.readInt()
            val serverRevision = ServerConfig.SERVER[ServerSpec.revision]

            if(revision != serverRevision) {
                logger.info("Connection from channel {} did not match the server's revision.", ctx.channel())
                ctx.writeAndFlush(ServerResponse.REVISION_MISMATCH).addListener(ChannelFutureListener.CLOSE)
            } else {
                setState(JS5DecoderState.JS5_ARCHIVE)
                ctx.writeAndFlush(ServerResponse.ACCEPTABLE)
            }
        }
    }

    private fun decodeArchive(buf: ByteBuf, out: MutableList<Any>) {
        if(!buf.isReadable) return

        buf.markReaderIndex()
        val opcode = buf.readByte().toInt()
        when(opcode) {
            CLIENT_INIT_GAME, CLIENT_LOAD_SCREEN, CLIENT_INIT_OPCODE -> {
                buf.skipBytes(3)
            }

            ARCHIVE_REQUEST_NEUTRAL, ARCHIVE_REQUEST_URGENT -> {
                if(buf.readableBytes() >= 3) {
                    val index = buf.readUnsignedByte().toInt()
                    val archive = buf.readUnsignedShort()

                    val request = JS5Request(index, archive, opcode == ARCHIVE_REQUEST_URGENT)
                    out.add(request)
                } else {
                    buf.resetReaderIndex()
                }
            }

            else -> {
                logger.error("Unhandled opcode: $opcode")
            }
        }
    }

    companion object : KLogging() {
        private const val ARCHIVE_REQUEST_URGENT = 0
        private const val ARCHIVE_REQUEST_NEUTRAL = 1
        private const val CLIENT_INIT_GAME = 2
        private const val CLIENT_LOAD_SCREEN = 3
        private const val CLIENT_INIT_OPCODE = 6
    }
}