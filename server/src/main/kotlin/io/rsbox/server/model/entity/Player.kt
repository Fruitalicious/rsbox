package io.rsbox.server.model.entity

import io.rsbox.api.inter.*
import io.rsbox.server.Server
import io.rsbox.server.def.impl.VarbitDef
import io.rsbox.server.def.impl.VarpDef
import io.rsbox.server.model.Appearance
import io.rsbox.server.model.data.varp.VarpSet
import io.rsbox.server.model.world.World
import io.rsbox.server.net.packet.Message
import io.rsbox.server.net.packet.impl.message.InterfaceOpenSubMessage
import io.rsbox.server.net.packet.impl.message.InterfaceOpenTopMessage
import io.rsbox.server.net.packet.impl.message.RunClientScriptMessage
import io.rsbox.server.sync.block.UpdateBlockType

/**
 * @author Kyle Escobar
 */

open class Player : LivingEntity(), io.rsbox.api.entity.Player {
    override var username = ""

    override var displayName = ""

    override var password = ""

    override var uuid = ""

    override var privilege = 0

    lateinit var client: Client

    var appearance: Appearance = Appearance.DEFAULT

    var lastIndex = -1

    /**
     * Rendering data
     */
    internal val gpiLocalPlayers = arrayOfNulls<Player>(2048)

    internal val gpiLocalIndexes = IntArray(2048)

    internal var gpiLocalCount = 0

    internal val gpiExternalIndexes = IntArray(2048)

    internal var gpiExternalCount = 0

    internal val gpiInactivityFlags = IntArray(2048)

    internal val gpiTileHashMultipliers = IntArray(2048)

    /**
     * Interface
     */
    override val interfaces by lazy { InterfaceSet() }

    override fun openOverlayInterface(displayMode: DisplayMode) {
        if(displayMode != interfaces.displayMode) {
            interfaces.setVisible(getDisplayComponentId(interfaces.displayMode), getChildId(InterfaceDestination.MAIN_SCREEN, interfaces.displayMode), false)
        }
        val component = getDisplayComponentId(displayMode)
        interfaces.setVisible(getDisplayComponentId(displayMode), 0, true)
        write(InterfaceOpenTopMessage(component))
    }

    override fun getInterfaceAt(dest: InterfaceDestination): Int {
        val displayMode = interfaces.displayMode
        val child = getChildId(dest, displayMode)
        val parent = getDisplayComponentId(displayMode)
        return interfaces.getInterfaceAt(parent, child)
    }

    override fun openInterface(dest: InterfaceDestination, autoClose: Boolean) {
        val displayMode = if(!autoClose || dest.fullscreenChildId == -1) interfaces.displayMode else DisplayMode.FULLSCREEN
        val child = getChildId(dest, displayMode)
        val parent = getDisplayComponentId(displayMode)
        if(displayMode == DisplayMode.FULLSCREEN) {
            openOverlayInterface(displayMode)
        }
        openInterface(parent, child, dest.interfaceId, if(dest.clickThrough) 1 else 0, isModal = dest == InterfaceDestination.MAIN_SCREEN)
    }

    override fun openInterface(parent: Int, child: Int, interfaceId: Int, type: Int, isModal: Boolean) {
        if(isModal) {
            interfaces.openModal(parent, child, interfaceId)
        } else {
            interfaces.open(parent, child, interfaceId)
        }
        write(InterfaceOpenSubMessage(parent, child, interfaceId, type))
    }

    override fun openInterface(interfaceId: Int, dest: InterfaceDestination, fullscreen: Boolean) {
        val displayMode = if(!fullscreen || dest.fullscreenChildId == -1) interfaces.displayMode else DisplayMode.FULLSCREEN
        val child = getChildId(dest, displayMode)
        val parent = getDisplayComponentId(displayMode)
        if(displayMode == DisplayMode.FULLSCREEN) {
            openOverlayInterface(displayMode)
        }
        openInterface(parent, child, interfaceId, if(dest.clickThrough) 1 else 0, isModal = dest == InterfaceDestination.MAIN_SCREEN)
    }

    /**
     * Send Packets
     */
    open fun handleMessages() {}

    open fun write(vararg messages: Message) {}

    open fun write(vararg messages: Any) {}

    open fun channelFlush() {}

    open fun channelClose() {}

    /**
     * Varps / Varbits
     */
    private val varps = VarpSet(maxVarps = (server as Server).definitions.getCount(VarpDef::class.java))

    override fun getVarbit(id: Int): Int {
        val def = (server as Server).definitions.get(VarbitDef::class.java, id)
        return varps.getBit(def.varp, def.startBit, def.endBit)
    }

    override fun getVarp(id: Int): Int = varps.getState(id)

    override fun setVarp(id: Int, value: Int) {
        varps.setState(id, value)
    }

    override fun setVarbit(id: Int, value: Int) {
        val def = (server as Server).definitions.get(VarbitDef::class.java, id)
        varps.setBit(def.varp, def.startBit, def.endBit, value)
    }

    override fun syncVarp(id: Int) {
        setVarp(id, getVarp(id))
    }

    /**
     * Block Update Methods
     */

    fun addBlock(block: UpdateBlockType) {
        val bits = (world as World).playerUpdateBlocks.updateBlocks[block]!!
        blockBuffer.addBit(bits.bit)
    }

    fun hasBlock(block: UpdateBlockType): Boolean {
        val bits = (world as World).playerUpdateBlocks.updateBlocks[block]!!
        return blockBuffer.hasBit(bits.bit)
    }

    /**
     * Helper Methods
     */
    override fun runClientScript(id: Int, vararg args: Any) {
        write(RunClientScriptMessage(id, *args))
    }

    /**
     * Called every game tick. Handles all the processing for the next tick for the player
     * object.
     */
    fun cycle() {

    }
}