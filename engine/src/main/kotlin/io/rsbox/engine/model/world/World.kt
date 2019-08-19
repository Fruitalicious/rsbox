package io.rsbox.engine.model.world

import io.rsbox.engine.Engine
import io.rsbox.engine.model.LivingEntityList
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.service.ServiceManager
import io.rsbox.engine.service.impl.XteaKeyService
import io.rsbox.util.HuffmanCodec
import net.runelite.cache.IndexType
import java.security.SecureRandom
import java.util.*

/**
 * @author Kyle Escobar
 */

class World(val engine: Engine) {

    val random: Random = SecureRandom()

    val players = LivingEntityList(arrayOfNulls<Player>(2000))

    val xteaKeyService: XteaKeyService = ServiceManager[XteaKeyService::class.java]!!

    val huffman by lazy {
        val binary = Engine.cacheStore.getIndex(IndexType.BINARY)!!
        val archive = binary.findArchiveByName("huffman")!!
        val file = archive.getFiles(Engine.cacheStore.storage.loadArchive(archive)!!).files[0]
        HuffmanCodec(file.contents)
    }

    fun init() {
        this.preLoad()
        this.load()
        this.postLoad()
    }

    private fun preLoad() {

    }

    private fun load() {

    }

    private fun postLoad() {

    }

    internal fun registerPlayer(player: Player): Boolean {
        val registered = players.add(player)
        if(registered) {
            player.lastIndex = player.index
            return true
        }
        return false
    }

    internal fun unregisterPlayer(player: Player): Boolean {
        return players.remove(player)
    }
}