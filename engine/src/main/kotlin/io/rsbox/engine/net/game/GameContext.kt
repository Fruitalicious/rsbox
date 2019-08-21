package io.rsbox.engine.net.game

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.rsbox.engine.net.ServerContext
import io.rsbox.engine.net.packet.ClientPacket
import io.rsbox.engine.net.packet.ServerPacket
import io.rsbox.engine.net.packet.model.GamePacket
import io.rsbox.engine.service.ServiceManager
import io.rsbox.engine.service.impl.GameService
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * @author Kyle Escobar
 */

class GameContext(channel: Channel) : ServerContext(channel) {

    val service = ServiceManager[GameService::class.java]!!

    private val packetQueue: BlockingQueue<ClientPacket> = ArrayBlockingQueue<ClientPacket>(50)

    override fun receiveMessage(ctx: ChannelHandlerContext, msg: Any) {
        if(msg is GamePacket) {
            val packet = service.packets.getClientPacket(msg.opcode)!!
            packet.fromGamePacket(msg)
            packetQueue.add(packet)
        }
    }

    override fun terminate() {

    }

    fun write(packet: ServerPacket) {
        channel.write(packet.toGamePacket())
    }

    fun handleMessages() {
        for(i in 0 until 50) {
            val next = packetQueue.poll() ?: break
            next.handler()
        }
    }

    fun flush() {
        if(channel.isActive) {
            channel.flush()
        }
    }

    fun close() {
        channel.disconnect()
    }
}