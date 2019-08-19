package io.rsbox.engine.net.packet

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import mu.KLogging

class PacketSet {

    private val serverPackets = arrayOfNulls<Class<out ServerPacket>>(256)

    private val clientPackets = arrayOfNulls<ClientPacket>(256)

    fun registerServerPacket(opcode: Int, packet: Class<out ServerPacket>) {
        if(serverPackets[opcode] != null) {
            logger.error("Unable to add packet {} with opcode {} as that opcode has already been bound.", packet::class.java.simpleName, opcode)
        } else {
            serverPackets[opcode] = packet
        }
    }

    fun registerClientPacket(opcode: Int, packet: ClientPacket) {
        if(clientPackets[opcode] != null) {
            logger.error("Unable to add packet {} with opcode {} as that opcode has already been bound.", packet::class.java.simpleName, opcode)
        } else {
            clientPackets[opcode] = packet
        }
    }

    fun getServerPacket(opcode: Int): ServerPacket? = serverPackets[opcode]!!.newInstance() ?: null

    fun getClientPacket(opcode: Int): ClientPacket? = clientPackets[opcode]

    fun validClientPacketOpcode(opcode: Int): Boolean = clientPackets[opcode] != null

    companion object : KLogging()
}

