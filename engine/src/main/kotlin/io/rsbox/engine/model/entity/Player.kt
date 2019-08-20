package io.rsbox.engine.model.entity

import io.netty.channel.Channel
import io.rsbox.api.inter.DisplayMode
import io.rsbox.api.inter.InterfaceDestination
import io.rsbox.api.inter.getChildId
import io.rsbox.api.inter.getDisplayComponentId
import io.rsbox.engine.Engine
import io.rsbox.engine.model.inter.InterfaceSet
import io.rsbox.engine.model.world.Coordinate
import io.rsbox.engine.net.game.GameContext
import io.rsbox.engine.net.packet.ServerPacket
import io.rsbox.engine.net.packet.impl.server.InterfaceOpenPacket
import io.rsbox.engine.net.packet.impl.server.InterfaceOpenRootPacket
import io.rsbox.engine.net.packet.impl.server.LoginPacket
import io.rsbox.engine.net.packet.model.GamePacket
import io.rsbox.engine.task.sync.block.UpdateBlockBuffer
import io.rsbox.engine.task.sync.block.UpdateBlockType
import io.rsbox.game.Game

/**
 * @author Kyle Escobar
 */

class Player(val channel: Channel) : LivingEntity(), io.rsbox.api.entity.Player {

    override lateinit var username: String

    override lateinit var passwordHash: String

    override lateinit var displayName: String

    override var privilege: Int = 0


    override var initiated = false

    var lastIndex = -1

    val _world = Engine.world
    override val world: io.rsbox.api.world.World = _world

    lateinit var context: GameContext

    internal val gpiLocalPlayers = arrayOfNulls<Player>(2048)
    internal val gpiLocalIndexes = IntArray(2048)
    internal var gpiLocalCount = 0

    internal val gpiExternalIndexes = IntArray(2048)
    internal var gpiExternalCount = 0
    internal val gpiInactivityFlags = IntArray(2048)

    internal val gpiTileHashMultipliers = IntArray(2048)

    var lastKnownRegionBase: Coordinate? = null

    val interfaces by lazy { InterfaceSet() }

    override val displayMode: DisplayMode get() = interfaces.displayMode

    val blockBuffer: UpdateBlockBuffer = UpdateBlockBuffer()

    fun register() {
        _world.registerPlayer(this)
    }

    fun login() {
        gpiLocalPlayers[index] = this
        gpiLocalIndexes[gpiLocalCount++] = index

        for(i in 1 until 2048) {
            if(i == index) continue
            gpiExternalIndexes[gpiExternalCount++] = i
            gpiTileHashMultipliers[i] = if(i < _world.players.capacity) _world.players[i]?._tile?.asTileHashMultiplier ?: 0 else 0
        }

        val tiles = IntArray(gpiTileHashMultipliers.size)
        System.arraycopy(gpiTileHashMultipliers, 0, tiles, 0, tiles.size)

        initiated = true

        sendPacket(LoginPacket(lastIndex, _tile, tiles, _world.xteaKeyService))

        Game.setupRootInterfaces(this)

        addBlock(UpdateBlockType.APPEARANCE)
    }

    fun sendPacket(packet: ServerPacket) {
        context.write(packet)
    }

    fun sendPacket(packet: GamePacket) {
        channel.write(packet)
    }

    fun addBlock(block: UpdateBlockType) {
        blockBuffer.addBit(block.mask)
    }

    fun hasBlock(block: UpdateBlockType): Boolean {
        return blockBuffer.hasBit(block.mask)
    }

    /**
     * INTERFACES
     */

    override fun openOverlayInterface(displayMode: DisplayMode) {
        if(displayMode != interfaces.displayMode) {
            interfaces.setVisible(getDisplayComponentId(interfaces.displayMode), getChildId(InterfaceDestination.MAIN_SCREEN, interfaces.displayMode), false)
        }

        val component = getDisplayComponentId(displayMode)
        interfaces.setVisible(getDisplayComponentId(displayMode), 0, true)
        sendPacket(InterfaceOpenRootPacket(component))
    }

    override fun openInterface(parent: Int, child: Int, interfaceId: Int, type: Int, isModal: Boolean) {
        if(isModal) {
            interfaces.openModal(parent, child, interfaceId)
        } else {
            interfaces.open(parent, child, interfaceId)
        }
        sendPacket(InterfaceOpenPacket(parent, child, interfaceId, type))
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

    override fun openInterface(dest: InterfaceDestination, autoClose: Boolean) {
        val displayMode = if(!autoClose || dest.fullscreenChildId == -1) interfaces.displayMode else DisplayMode.FULLSCREEN
        val child = getChildId(dest, displayMode)
        val parent = getDisplayComponentId(displayMode)
        if(displayMode == DisplayMode.FULLSCREEN) {
            openOverlayInterface(displayMode)
        }
        openInterface(parent, child, dest.interfaceId, if(dest.clickThrough) 1 else 0, isModal = dest == InterfaceDestination.MAIN_SCREEN)
    }

    companion object {
        /**
         * How many tiles a player can 'see' at a time, normally.
         */
        const val NORMAL_VIEW_DISTANCE = 15

        /**
         * How many tiles a player can 'see' at a time when in a 'large' viewport.
         */
        const val LARGE_VIEW_DISTANCE = 127

        /**
         * How many tiles in each direction a player can see at a given time.
         * This should be as far as players can see entities such as ground items
         * and objects.
         */
        const val TILE_VIEW_DISTANCE = 32
    }
}