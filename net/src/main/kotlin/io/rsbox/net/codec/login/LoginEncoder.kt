package io.rsbox.net.codec.login

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.net.login.LoginResponse
import io.rsbox.net.codec.game.GamePacketDecoder
import io.rsbox.net.codec.game.GamePacketEncoder
import io.rsbox.net.context.ContextHandler
import io.rsbox.net.context.GameContext
import io.rsbox.util.IsaacRandom

/**
 * @author Kyle Escobar
 */

class LoginEncoder : MessageToByteEncoder<LoginResponse>() {
    override fun encode(ctx: ChannelHandlerContext, msg: LoginResponse, out: ByteBuf) {
        out.writeByte(2)
        out.writeByte(13)
        out.writeByte(0)
        out.writeInt(0)
        out.writeByte(msg.player.privilege)
        out.writeBoolean(true)
        out.writeShort(msg.player.index)
        out.writeBoolean(true)
    }
}