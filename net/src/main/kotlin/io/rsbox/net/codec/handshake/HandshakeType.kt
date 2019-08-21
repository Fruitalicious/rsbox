package io.rsbox.net.codec.handshake

/**
 * @author Kyle Escobar
 */

enum class HandshakeType(val opcode: Int) {
    JS5(15),
    LOGIN(14);

    companion object {
        val values = enumValues<HandshakeType>()
    }
}