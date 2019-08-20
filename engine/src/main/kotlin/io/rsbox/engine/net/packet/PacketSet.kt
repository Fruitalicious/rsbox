package io.rsbox.engine.net.packet

import io.rsbox.engine.net.packet.impl.server.LoginPacket
import io.rsbox.engine.net.packet.impl.server.RebuildRegionPacket
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import mu.KLogging

class PacketSet {

    private val serverPackets = arrayOfNulls<Class<out ServerPacket>>(256)

    private val clientPackets = arrayOfNulls<ClientPacket>(256)

    fun loadPackets() {
        // Server Packets
        registerServerPacket(0, LoginPacket::class.java)
        registerServerPacket(0, RebuildRegionPacket::class.java)

        // Client Packets
    }

    private fun registerServerPacket(opcode: Int, packet: Class<out ServerPacket>) {
        serverPackets[opcode] = packet
    }

    private fun registerClientPacket(opcode: Int, packet: ClientPacket) {
        if(clientPackets[opcode] != null) {
            logger.error("Unable to add packet {} with opcode {} as that opcode has already been bound.", packet::class.java.simpleName, opcode)
        } else {
            clientPackets[opcode] = packet
        }
    }

    fun getServerPacket(opcode: Int): ServerPacket? = serverPackets[opcode]!!.newInstance() ?: null

    fun getClientPacket(opcode: Int): ClientPacket? {
        if(opcode < 0 || opcode > 256) return null
        return clientPackets[opcode]
    }

    companion object : KLogging()
}

