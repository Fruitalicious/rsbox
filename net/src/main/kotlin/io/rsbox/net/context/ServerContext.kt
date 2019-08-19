package io.rsbox.net.context

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext

/**
 * @author Kyle Escobar
 */

abstract class ServerContext(open val channel: Channel) {
    abstract fun receiveMessage(ctx: ChannelHandlerContext, msg: Any)

    abstract fun terminate()
}