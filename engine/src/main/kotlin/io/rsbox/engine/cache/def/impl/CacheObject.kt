package io.rsbox.engine.cache.def.impl

import io.netty.buffer.ByteBuf
import io.rsbox.engine.cache.def.CacheDef
import io.rsbox.util.BufferUtils.readString
import net.runelite.cache.ConfigType

/**
 * @author Tom <rspsmods@gmail.com>
 */
class CacheObject(override val id: Int, override val configType: ConfigType = ConfigType.OBJECT) : CacheDef(id, configType) {

    var name = ""
    var width = 1
    var length = 1
    var solid = true
    var impenetrable = true
    var interactive = false
    var obstructive = false
    var clipMask = 0
    var varbit = -1
    var varp = -1
    var animation = -1
    var rotated = false
    val options: Array<String?> = Array(5) { "" }
    var transforms: Array<Int>? = null

    var examine: String? = null

    override fun decode(buf: ByteBuf, opcode: Int) {
        when (opcode) {
            1 -> {
                val count = buf.readUnsignedByte()
                for (i in 0 until count) {
                    buf.readUnsignedShort() // Model
                    buf.readUnsignedByte() // Model type
                }
            }
            2 -> name = buf.readString()
            5 -> {
                val count = buf.readUnsignedByte()
                for (i in 0 until count) {
                    buf.readUnsignedShort() // Model
                }
            }
            14 -> width = buf.readUnsignedByte().toInt()
            15 -> length = buf.readUnsignedByte().toInt()
            17 -> solid = false
            18 -> impenetrable = false
            19 -> interactive = buf.readUnsignedByte().toInt() == 1
            24 -> {
                animation = buf.readUnsignedShort()
                if (animation == 65535) {
                    animation = -1
                }
            }
            27 -> {}
            28 -> buf.readUnsignedByte()
            29 -> buf.readByte()
            in 30 until 35 -> {
                options[opcode - 30] = buf.readString()
                if (options[opcode - 30]?.toLowerCase() == "null") {
                    options[opcode - 30] = null
                }
            }
            39 -> buf.readByte()
            40 -> {
                val count = buf.readUnsignedByte()
                for (i in 0 until count) {
                    buf.readUnsignedShort() // Recolor src
                    buf.readUnsignedShort() // Recolor dst
                }
            }
            41 -> {
                val count = buf.readUnsignedByte()
                for (i in 0 until count) {
                    buf.readUnsignedShort() // Retexture src
                    buf.readUnsignedShort() // Retexture dst
                }
            }
            60 -> buf.readUnsignedShort()
            62 -> rotated = true
            65 -> buf.readUnsignedShort()
            66 -> buf.readUnsignedShort()
            67 -> buf.readUnsignedShort()
            68 -> buf.readUnsignedShort()
            69 -> clipMask = buf.readUnsignedByte().toInt()
            70 -> buf.readShort()
            71 -> buf.readShort()
            72 -> buf.readShort()
            73 -> obstructive = true
            75 -> buf.readUnsignedByte()
            77, 92 -> {
                varbit = buf.readUnsignedShort()
                varp = buf.readUnsignedShort()

                if (varbit == 65535) {
                    varbit = -1
                }
                if (varp == 65535) {
                    varp = -1
                }

                if (opcode == 92) {
                    buf.readUnsignedShort()
                }

                val count = buf.readUnsignedByte().toInt()

                transforms = Array(count + 1) { 0 }
                for (i in 0..count) {
                    val transform = buf.readUnsignedShort()
                    transforms!![i] = transform
                }
            }
            78 -> {
                buf.readUnsignedShort()
                buf.readUnsignedByte()
            }
            79 -> {
                buf.readUnsignedShort()
                buf.readUnsignedShort()
                buf.readUnsignedByte()
                val count = buf.readUnsignedByte().toInt()
                for (i in 0 until count) {
                    buf.readUnsignedShort()
                }
            }
            81 -> buf.readUnsignedByte()
            82 -> buf.readUnsignedShort()
            249 -> readParams(buf)
        }
    }
}