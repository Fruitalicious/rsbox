package io.rsbox.engine.net.packet.impl.server

import io.rsbox.engine.net.packet.ServerPacket
import io.rsbox.engine.net.packet.model.DataOrder
import io.rsbox.engine.net.packet.model.DataTransformation
import io.rsbox.engine.net.packet.model.DataType
import io.rsbox.engine.net.packet.model.PacketType

/**
 * @author Kyle Escobar
 */

class InterfaceOpenPacket(val parent: Int, val child: Int, val component: Int, val iftype: Int) : ServerPacket() {
    override var opcode = 77
    override var type = PacketType.FIXED

    override fun encode() {
        initPacket()

        // INTERFACE TYPE
        packet.put(type = DataType.BYTE, transformation = DataTransformation.ADD, value = iftype)

        // OVERLAY
        packet.put(type = DataType.INT, order = DataOrder.MIDDLE, value = (parent shl 16) or child)

        // COMPONENT
        packet.put(type = DataType.SHORT, order = DataOrder.LITTLE, transformation = DataTransformation.ADD, value = component)
    }
}