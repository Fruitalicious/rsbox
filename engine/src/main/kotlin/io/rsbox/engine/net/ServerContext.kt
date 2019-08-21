package io.rsbox.engine.net

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext

/**
 * @author Kyle Escobar
 */

abstract class ServerContext(open val channel: Channel) {
    abstract fun receiveMessage(ctx: ChannelHandlerContext, msg: Any)

    abstract fun terminate()
}