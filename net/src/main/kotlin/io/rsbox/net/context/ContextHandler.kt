package io.rsbox.net.context

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.ReadTimeoutException
import io.netty.util.AttributeKey
import io.rsbox.engine.net.ServerContext
import io.rsbox.net.codec.handshake.HandshakeMessage
import io.rsbox.net.codec.handshake.HandshakeType
import mu.KLogging

/**
 * @author Kyle Escobar
 */

@ChannelHandler.Sharable
class ContextHandler : ChannelInboundHandlerAdapter() {
    override fun channelInactive(ctx: ChannelHandlerContext) {
        val session = ctx.channel().attr(CONTEXT_KEY).andRemove
        session?.terminate()
        ctx.channel().close()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if(cause.stackTrace.isEmpty() || cause.stackTrace[0].methodName != "read0") {
            if(cause is ReadTimeoutException) {
                logger.info("Channel disconnect due to a timeout: {}", ctx.channel())
            } else {
                logger.error("Channel threw an error: ${ctx.channel()}.", cause)
            }
        }
        ctx.channel().close()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        try {
            val attribute = ctx.channel().attr(CONTEXT_KEY)
            val context = attribute.get()

            if(context != null) {
                context.receiveMessage(ctx,msg)
            }
            else if(msg is HandshakeMessage) {
                when(msg.opcode) {
                    HandshakeType.JS5.opcode -> ctx.channel().attr(CONTEXT_KEY).set(JS5Context(ctx.channel()))
                    HandshakeType.LOGIN.opcode -> ctx.channel().attr(CONTEXT_KEY).set(LoginContext(ctx.channel()))
                }
            }

        } catch(e : Exception) {
            logger.error("Error when processing message: $msg, channel: ${ctx.channel()}", e)
        }
    }

    companion object : KLogging() {
        val CONTEXT_KEY: AttributeKey<ServerContext> = AttributeKey.valueOf("context")
    }
}