package io.rsbox.engine.config

import com.uchuhimo.konf.Config
import java.io.File

/**
 * @author Kyle Escobar
 */

object ServerConfig {
    lateinit var SERVER: Config
    lateinit var GAME: Config
    lateinit var DEV: Config

    val SERVER_PATH = File("rsbox/config/server.yml")
    val GAME_PATH = File("rsbox/config/game.yml")
    val DEV_PATH = File("rsbox/config/dev.yml")
}