package io.rsbox.engine

import io.rsbox.engine.crypt.rsa.RSA
import io.rsbox.engine.model.world.World
import io.rsbox.engine.service.Service
import io.rsbox.engine.service.ServiceManager
import mu.KLogging
import net.runelite.cache.fs.Store
import java.io.File

/**
 * @author Kyle Escobar
 */

class Engine {

    fun preInit() {}

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

        ServiceManager.init()

        logger.info("Preparing to load world.")
        world = World(this)
        world.init()
        logger.info("Loaded game world.")
    }

    fun postInit() {

    }

    companion object : KLogging() {

        lateinit var cacheStore: Store

        lateinit var world: World

    }
}