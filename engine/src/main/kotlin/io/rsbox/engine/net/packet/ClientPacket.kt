package io.rsbox.engine.net.packet

import io.rsbox.engine.net.packet.model.GamePacket
import io.rsbox.engine.net.packet.model.GamePacketReader
import io.rsbox.engine.net.packet.model.PacketType

abstract class ClientPacket {
    open var opcode: Int = -1
    open var type: PacketType = PacketType.RAW

    lateinit var packet: GamePacket
    lateinit var reader: GamePacketReader

    fun fromGamePacket(packet: GamePacket) {
        this.packet = packet
        this.reader = GamePacketReader(this.packet)
    }

    abstract fun decode()

    abstract fun handler()
}