package io.rsbox.net.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import java.util.*

/**
 * @author Kyle Escobar
 */

abstract class StatefulMessageDecoder<T : Enum<T>>(private var state: T) : ByteToMessageDecoder() {
    fun setState(state: T) {
        this.state = Objects.requireNonNull(state, "State cannot be null.")
    }

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        decode(ctx, buf, out, state)
    }

    abstract fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>, state: T)
}