package io.rsbox.engine.cache.def.impl

import io.netty.buffer.ByteBuf
import io.rsbox.engine.cache.def.CacheDef
import net.runelite.cache.ConfigType

/**
 * @author Tom <rspsmods@gmail.com>
 */
class CacheVarp(override val id: Int, override val configType: ConfigType = ConfigType.VARPLAYER) : CacheDef(id, configType) {

    var cacheConfigType = 0

    override fun decode(buf: ByteBuf, opcode: Int) {
        when (opcode) {
            5 -> cacheConfigType = buf.readUnsignedShort()
        }
    }
}