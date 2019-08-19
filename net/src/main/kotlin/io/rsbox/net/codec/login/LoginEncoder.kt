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

        setupGameContext(ctx, msg.player, msg.encodeRandom, msg.decodeRandom)
    }

    private fun setupGameContext(ctx: ChannelHandlerContext, player: Player, encodeRandom: IsaacRandom, decodeRandom: IsaacRandom) {
        val gameContext = GameContext(ctx.channel(), player)
        player.channel.attr(ContextHandler.CONTEXT_KEY).set(gameContext)

        val p = player.channel.pipeline()

        if(player.channel.isActive) {
            p.remove("handshake_encoder")
            p.remove("login_decoder")
            p.remove("login_encoder")

            p.addFirst("packet_encoder", GamePacketEncoder(encodeRandom))
            p.addBefore("handler", "packet_decoder", GamePacketDecoder(decodeRandom))

            player.login()
            player.channel.flush()
        }
    }
}