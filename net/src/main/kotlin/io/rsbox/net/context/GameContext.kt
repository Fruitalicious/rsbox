package io.rsbox.net.context

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.rsbox.engine.model.entity.Player

/**
 * @author Kyle Escobar
 */

class GameContext(channel: Channel, player: Player) : ServerContext(channel) {
    override fun receiveMessage(ctx: ChannelHandlerContext, msg: Any) {

    }

    override fun terminate() {

    }
}