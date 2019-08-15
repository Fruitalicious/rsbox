package io.rsbox.server.model.world

import io.rsbox.api.world.World
import io.rsbox.server.Server
import io.rsbox.server.model.LivingEntityList
import io.rsbox.server.model.entity.Npc
import io.rsbox.server.model.entity.Player
import io.rsbox.server.service.impl.XteaKeyService
import io.rsbox.server.sync.block.UpdateBlockSet
import io.rsbox.server.sync.block.impl.PlayerUpdateBlock
import mu.KLogging
import java.security.SecureRandom
import java.util.*

/**
 * @author Kyle Escobar
 */

class World(val server: Server) : World {

    val players = LivingEntityList<Player>(arrayOfNulls(2048))

    val npcs = LivingEntityList<Npc>(arrayOfNulls(25565))

    internal val playerUpdateBlocks = UpdateBlockSet()

    internal val npcUpdateBlocks = UpdateBlockSet()

    val random = SecureRandom()

    var currentCycle = 0

    lateinit var xteaKeyService: XteaKeyService


    /**
     * World base methods
     */
    override fun init() {
        load()
    }

    override fun load() {

        /**
         * Load Update Blocks
         */
        playerUpdateBlocks.load(PlayerUpdateBlock())

        logger.info { "Loaded segment blocks."}
    }

    override fun unload() {

    }


    /**
     * Registers player objects with the world.
     * Sets the player index.
     */
    fun register(player: Player): Boolean {
        val registered = players.add(player)
        if(registered) {
            player.lastIndex = player.index
            return true
        }
        return false
    }

    override fun register(player: io.rsbox.api.entity.Player): Boolean {
        return this.register(player as Player)
    }

    companion object : KLogging()
}