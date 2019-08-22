package io.rsbox.engine.cache.def

import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import io.rsbox.util.BufferUtils.readString
import net.runelite.cache.ConfigType

/**
 * @author Kyle Escobar
 */

abstract class CacheDef(open val id: Int, open val configType: ConfigType) : io.rsbox.api.cache.CacheDef {
    fun decode(buf: ByteBuf) {
        while(true) {
            val opcode = buf.readUnsignedByte().toInt()
            if(opcode == 0) {
                break
            }
            decode(buf, opcode)
        }
    }

    abstract fun decode(buf: ByteBuf, opcode: Int)

    fun readParams(buf: ByteBuf) : Int2ObjectOpenHashMap<Any> {
        val map = Int2ObjectOpenHashMap<Any>()

        val length = buf.readUnsignedByte()
        for(i in 0 until length) {
            val isString = buf.readUnsignedByte().toInt() == 1
            val id = buf.readUnsignedMedium()
            if(isString) {
                map[id] = buf.readString()
            } else {
                map[id] = buf.readInt()
            }
        }
        return map
    }
}