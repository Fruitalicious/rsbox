package io.rsbox.engine.net.packet

import io.rsbox.engine.net.packet.model.GamePacket
import io.rsbox.engine.net.packet.model.GamePacketBuilder
import io.rsbox.engine.net.packet.model.PacketType

/**
 * @author Kyle Escobar
 */

abstract class ServerPacket {

    open var opcode: Int = -1
    open var type: PacketType = PacketType.RAW

    lateinit var packet: GamePacketBuilder

    fun initPacket() {
        packet = GamePacketBuilder(opcode, type)
    }

    abstract fun encode()

    fun toGamePacket(): GamePacket {
        this.encode()
        return packet.toGamePacket()
    }
}