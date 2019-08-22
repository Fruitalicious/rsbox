package io.rsbox.engine

import io.rsbox.api.RSBox
import io.rsbox.engine.cache.def.CacheData
import io.rsbox.engine.system.crypt.rsa.RSA
import io.rsbox.engine.model.world.World
import io.rsbox.engine.service.ServiceManager
import mu.KLogging
import net.runelite.cache.fs.Store
import java.io.File

/**
 * @author Kyle Escobar
 */

class Engine : io.rsbox.api.Engine {

    lateinit var _cache: CacheData
    override val cache: io.rsbox.api.cache.CacheData get() = _cache

    fun preInit() {
        RSBox.engine = this
    }

    fun init() {
        logger.info("Loading cache from data store.")
        cacheStore = Store(File(EngineConstants.CACHE_PATH))
        cacheStore.load()

        if(cacheStore.indexes.size == 0) {
            logger.error("There are no cache files in the ${EngineConstants.CACHE_PATH} directory.")
            logger.warn("Make sure you run the gradle [server:setup] task before starting this server.")
            System.exit(0)
        }

        logger.info("Preparing to load RSA key pairs.")
        RSA.load()

        world = World(this)
        world.preLoad()
        world.load()

        ServiceManager.init()

        world.postLoad()
        logger.info("Loaded game world.")

        _cache = CacheData()
        _cache.loadAll()
    }

    fun postInit() {

    }

    companion object : KLogging() {

        lateinit var cacheStore: Store

        lateinit var world: World

    }
}