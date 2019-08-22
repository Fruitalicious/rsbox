package io.rsbox.engine.cache.def.impl

import io.netty.buffer.ByteBuf
import io.rsbox.engine.cache.def.CacheDef
import it.unimi.dsi.fastutil.bytes.Byte2ByteOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.runelite.cache.ConfigType
import io.rsbox.util.BufferUtils.readString

/**
 * @author Tom <rspsmods@gmail.com>
 */
class CacheItem(override val id: Int, override val configType: ConfigType = ConfigType.ITEM) : CacheDef(id, configType), io.rsbox.api.cache.types.CacheItem {

    override var name = ""
    override var canStack = false
    override var cost = 0
    override var isMembers = false
    override val groundMenu = Array<String?>(5) { null }
    override val inventoryMenu = Array<String?>(5) { null }
    override val equipmentMenu = Array<String?>(8) { null }
    /**
     * The item can be traded through the grand exchange.
     */
    override var isTradable = false
    override var teamCape = 0
    /**
     * When an item is noted or unnoted (and has a noted variant), this will
     * represent the other item id. For example, item definition [4151] will
     * have a [noteId] of [4152], while item definition [4152] will have
     * a [noteId] of 4151.
     */
    override var noteId = 0
    /**
     * When an item is noted, it will set this value.
     */
    override var noteTemplateId = 0
    override var placeholderId = 0
    override var placeholderTemplateId = 0

    val params = Int2ObjectOpenHashMap<Any>()

    /**
     * Custom metadata.
     */
    var examine: String? = null
    var tradeable = false
    var weight = 0.0
    var attackSpeed = -1
    var equipSlot = -1
    var equipType = 0
    var weaponType = -1
    var renderAnimations: IntArray? = null
    var skillReqs: Byte2ByteOpenHashMap? = null
    lateinit var bonuses: IntArray

    val stackable: Boolean
        get() = canStack || noteTemplateId > 0

    val noted: Boolean
        get() = noteTemplateId > 0

    /**
     * Whether or not the object is a placeholder.
     */
    val isPlaceholder
        get() = placeholderTemplateId > 0 && placeholderId > 0

    override fun decode(buf: ByteBuf, opcode: Int) {
        when (opcode) {
            1 -> buf.readUnsignedShort()
            2 -> name = buf.readString()
            4 -> buf.readUnsignedShort()
            5 -> buf.readUnsignedShort()
            6 -> buf.readUnsignedShort()
            7 -> buf.readUnsignedShort()
            8 -> buf.readUnsignedShort()
            11 -> canStack = true
            12 -> cost = buf.readInt()
            16 -> isMembers = true
            23 -> {
                buf.readUnsignedShort()
                buf.readUnsignedByte()
            }
            24 -> buf.readUnsignedShort()
            25 -> {
                buf.readUnsignedShort()
                buf.readUnsignedByte()
            }
            26 -> buf.readUnsignedShort()
            in 30 until 35 -> {
                groundMenu[opcode - 30] = buf.readString()
                if (groundMenu[opcode - 30]!!.toLowerCase() == "null") {
                    groundMenu[opcode - 30] = null
                }
            }
            in 35 until 40 -> inventoryMenu[opcode - 35] = buf.readString()
            40 -> {
                val count = buf.readUnsignedByte()

                for (i in 0 until count) {
                    buf.readUnsignedShort()
                    buf.readUnsignedShort()
                }
            }
            41 -> {
                val count = buf.readUnsignedByte()

                for (i in 0 until count) {
                    buf.readUnsignedShort()
                    buf.readUnsignedShort()
                }
            }
            42 -> buf.readByte()
            65 -> isTradable = true
            78 -> buf.readUnsignedShort()
            79 -> buf.readUnsignedShort()
            90 -> buf.readUnsignedShort()
            91 -> buf.readUnsignedShort()
            92 -> buf.readUnsignedShort()
            93 -> buf.readUnsignedShort()
            95 -> buf.readUnsignedShort()
            97 -> noteId = buf.readUnsignedShort()
            98 -> noteTemplateId = buf.readUnsignedShort()
            in 100 until 110 -> {
                buf.readUnsignedShort()
                buf.readUnsignedShort()
            }
            110 -> buf.readUnsignedShort()
            111 -> buf.readUnsignedShort()
            112 -> buf.readUnsignedShort()
            113 -> buf.readByte()
            114 -> buf.readByte()
            115 -> teamCape = buf.readUnsignedByte().toInt()
            139 -> buf.readUnsignedShort()
            140 -> buf.readUnsignedShort()
            148 -> placeholderId = buf.readUnsignedShort()
            149 -> placeholderTemplateId = buf.readUnsignedShort()
            249 -> {
                params.putAll(readParams(buf))

                for (i in 0 until 8) {
                    val paramId = 451 + i
                    val option = params.get(paramId) as? String ?: continue
                    equipmentMenu[i] = option
                }
            }
        }
    }
}