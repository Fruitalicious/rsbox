package io.rsbox.net.context

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.net.packet.ClientPacket
import io.rsbox.engine.net.packet.impl.server.LoginPacket
import io.rsbox.engine.net.packet.model.GamePacket
import io.rsbox.engine.service.ServiceManager
import io.rsbox.engine.service.impl.GameService
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * @author Kyle Escobar
 */

class GameContext(channel: Channel, player: Player) : ServerContext(channel) {

    val service = ServiceManager[GameService::class.java]!!

    override fun receiveMessage(ctx: ChannelHandlerContext, msg: Any) {
        if(msg is GamePacket) {
            val packet = service.packets.getClientPacket(msg.opcode)!!
            packet.fromGamePacket(msg)
            service.queueIngressPacket(packet)
        }
    }

    override fun terminate() {

    }
}