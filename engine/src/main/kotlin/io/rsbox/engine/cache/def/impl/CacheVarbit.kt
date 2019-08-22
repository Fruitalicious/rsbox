package io.rsbox.engine.cache.def.impl

import io.netty.buffer.ByteBuf
import io.rsbox.engine.cache.def.CacheDef
import net.runelite.cache.ConfigType

/**
 * @author Tom <rspsmods@gmail.com>
 */
class CacheVarbit(override val id: Int, override val configType: ConfigType = ConfigType.VARBIT) : CacheDef(id, configType) {

    var varp = 0
    var startBit = 0
    var endBit = 0

    override fun decode(buf: ByteBuf, opcode: Int) {
        when (opcode) {
            1 -> {
                varp = buf.readUnsignedShort()
                startBit = buf.readUnsignedByte().toInt()
                endBit = buf.readUnsignedByte().toInt()
            }
        }
    }
}