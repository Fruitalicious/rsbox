package io.rsbox.server.sync.block

import io.rsbox.server.model.data.chat.ChatMessage

/**
 * @author Kyle Escobar
 */

class UpdateBlockBuf {
    var teleport = false

    var mask = 0

    var forceChat = ""

    lateinit var publicChat: ChatMessage

    var faceDegrees = 0

    var faceEntityIndex = -1

    var animation = 0

    var animationDelay = 0

    var graphicId = 0

    var graphicHeight = 0

    var graphicDelay = 0

    fun hasChanged(): Boolean = mask != 0

    fun reset() {
        mask = 0
        teleport = false
    }

    fun addBit(bit: Int) {
        mask = mask or bit
    }

    fun hasBit(bit: Int): Boolean {
        return (mask and bit) != 0
    }

    fun blockValue(): Int = mask
}