package io.rsbox.server

/**
 * @author Kyle Escobar
 */

object Launcher {

    lateinit var server: Server

    @Suppress("UnusedMainParameter")
    @JvmStatic
    fun main(args: Array<String>) {
        server = Server()
        server.start()
    }
}