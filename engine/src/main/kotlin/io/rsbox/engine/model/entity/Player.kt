package io.rsbox.engine.model.entity

import io.netty.channel.Channel
import io.rsbox.engine.Engine
import io.rsbox.engine.net.packet.ServerPacket
import io.rsbox.engine.net.packet.impl.server.LoginPacket

/**
 * @author Kyle Escobar
 */

class Player(val channel: Channel) : LivingEntity() {

    lateinit var username: String

    lateinit var passwordHash: String

    lateinit var displayName: String

    var privilege: Int = 0


    var initiated = false

    var lastIndex = -1

    val world = Engine.world

    internal val gpiLocalPlayers = arrayOfNulls<Player>(2048)
    internal val gpiLocalIndexes = IntArray(2048)
    internal var gpiLocalCount = 0

    internal val gpiExternalIndexes = IntArray(2048)
    internal var gpiExternalCount = 0
    internal val gpiInactivityFlags = IntArray(2048)

    internal val gpiTileHashMultipliers = IntArray(2048)

    fun register() {
        world.registerPlayer(this)
    }

    fun login() {
        gpiLocalPlayers[index] = this
        gpiLocalIndexes[gpiLocalCount++] = index

        for(i in 1 until 2048) {
            if(i == index) continue
            gpiExternalIndexes[gpiExternalCount++] = i
            gpiTileHashMultipliers[i] = if(i < world.players.capacity) world.players[i]?.tile?.asTileHashMultiplier ?: 0 else 0
        }

        val tiles = IntArray(gpiTileHashMultipliers.size)
        System.arraycopy(gpiTileHashMultipliers, 0, tiles, 0, tiles.size)

        initiated = true

        sendPacket(LoginPacket(lastIndex, tile, tiles, world.xteaKeyService))
    }

    fun sendPacket(packet: ServerPacket) {
        channel.write(packet.toGamePacket())
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