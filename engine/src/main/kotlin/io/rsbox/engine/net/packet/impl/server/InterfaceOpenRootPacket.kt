package io.rsbox.engine.net.packet.impl.server

import io.rsbox.engine.net.packet.ServerPacket
import io.rsbox.engine.net.packet.model.DataOrder
import io.rsbox.engine.net.packet.model.DataTransformation
import io.rsbox.engine.net.packet.model.DataType
import io.rsbox.engine.net.packet.model.PacketType

/**
 * @author Kyle Escobar
 */

class InterfaceOpenRootPacket(val root: Int) : ServerPacket() {
    override var opcode = 84
    override var type = PacketType.FIXED

    override fun encode() {
        initPacket()

        // ROOT
        packet.put(type = DataType.SHORT, order = DataOrder.LITTLE, transformation = DataTransformation.ADD, value = root)
    }
}