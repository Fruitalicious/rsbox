package io.rsbox.engine.service.impl

import io.rsbox.engine.net.packet.ClientPacket
import io.rsbox.engine.net.packet.PacketSet
import io.rsbox.engine.net.packet.impl.server.LoginPacket
import io.rsbox.engine.service.Service
import java.util.concurrent.ArrayBlockingQueue

class GameService : Service() {

    val packets = PacketSet()

    private val ingressPacketQueue = ArrayBlockingQueue<ClientPacket>(50)

    override fun start() {
        registerPackets()
    }

    override fun stop() {

    }

    private fun registerPackets() {
        // Server Packets
        packets.registerServerPacket(0, LoginPacket::class.java)

        // Client Packets
    }

    fun queueIngressPacket(handle: ClientPacket) {
        ingressPacketQueue.offer(handle)
    }

    fun handlePackets() {
        for(i in 0 until 50) {
            val next = ingressPacketQueue.poll() ?: break
            next.decode()
            next.handler()
        }
    }
}