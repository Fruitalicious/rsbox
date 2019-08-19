package io.rsbox.server

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml.toYaml
import io.rsbox.engine.Engine
import io.rsbox.engine.EngineConstants
import io.rsbox.engine.config.ServerConfig
import io.rsbox.engine.config.spec.ServerSpec
import io.rsbox.net.LoginNetworkProcessor
import io.rsbox.net.ServerNetwork
import io.rsbox.server.setup.Setup
import mu.KLogging
import java.io.File

/**
 * @author Kyle Escobar
 */

class Server {

    /**
     * Entry into starting the server.
     */
    fun start() {

        logger.info { "Server startup initialization..." }
        this.init()
    }



    ////////////////////////////////////////////////////////////////////////////

    private var engine = Engine()

    private fun init() {
        logger.info("Scanning data directories.")
        initDirs()

        logger.info("Loading configs.")
        initConfigs()

        logger.info("Running engine pre-flight checks.")
        engine.preInit()

        logger.info("Initializing game engine.")
        engine.init()
        engine.postInit()

        LoginNetworkProcessor.start()

        logger.info("Starting game network.")
        ServerNetwork.start()
    }

    private fun initDirs() {
        Setup.dirs.forEach { dir ->
            val file = File(dir)
            if(!file.exists()) {
                logger.error("Your project need to be setup or updated. Please run the appropriate gradle task.")
                System.exit(0)
            }
        }
    }

    private fun initConfigs() {
        /**
         * Server Config
         */
        if(!ServerConfig.SERVER_PATH.exists()) {
            Config{addSpec(ServerSpec)}.toYaml.toFile(ServerConfig.SERVER_PATH)
            logger.info("Created default server.yml config as it did not exist.")
        }
        ServerConfig.SERVER = Config { addSpec(ServerSpec) }.from.yaml.file(ServerConfig.SERVER_PATH)
        ServerConfig.SERVER.toYaml.toFile(ServerConfig.SERVER_PATH)
        logger.info("Loaded config {}.", ServerConfig.SERVER_PATH.name)
    }


    companion object : KLogging()
}