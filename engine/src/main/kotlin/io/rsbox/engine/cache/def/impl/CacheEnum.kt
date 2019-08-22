package io.rsbox.engine.cache.def.impl

import io.netty.buffer.ByteBuf
import io.rsbox.engine.cache.def.CacheDef
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import io.rsbox.util.BufferUtils.readString
import net.runelite.cache.ConfigType

/**
 * @author Tom <rspsmods@gmail.com>
 */
class CacheEnum(override val id: Int, override val configType: ConfigType = ConfigType.ENUM) : CacheDef(id, configType), io.rsbox.api.cache.types.CacheEnum {

    override var keyType = 0

    override var valueType = 0

    override var defaultInt = 0

    override var defaultString = ""

    private val values = Int2ObjectOpenHashMap<Any>()

    override fun decode(buf: ByteBuf, opcode: Int) {
        when (opcode) {
            1 -> keyType = buf.readUnsignedByte().toInt()
            2 -> valueType = buf.readUnsignedByte().toInt()
            3 -> defaultString = buf.readString()
            4 -> defaultInt = buf.readInt()
            5, 6 -> {
                val count = buf.readUnsignedShort()
                for (i in 0 until count) {
                    val key = buf.readInt()
                    if (opcode == 5) {
                        values[key] = buf.readString()
                    } else {
                        values[key] = buf.readInt()
                    }
                }
            }
        }
    }

    override fun getInt(key: Int): Int = values[key] as? Int ?: defaultInt

    override fun getString(key: Int): String = values[key] as? String ?: defaultString
}