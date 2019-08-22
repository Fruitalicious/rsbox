package io.rsbox.server

import io.rsbox.engine.system.crypt.rsa.RSA
import io.rsbox.server.setup.Setup
import java.util.*


/**
 * @author Kyle Escobar
 */

object Launcher {

    lateinit var server: Server

    @JvmStatic
    fun main(args: Array<String>) {
        if(args.isNotEmpty() && args[0] == "--setup") {
            this.setup()
        } else {
            server = Server()
            server.start()
        }
    }

    private fun setup() {
        Setup.path = ""

        val scanner = Scanner(System.`in`)

        println("\n" +
                "  _____   _____ ____   ______   __\n" +
                " |  __ \\ / ____|  _ \\ / __ \\ \\ / /\n" +
                " | |__) | (___ | |_) | |  | \\ V / \n" +
                " |  _  / \\___ \\|  _ <| |  | |> <  \n" +
                " | | \\ \\ ____) | |_) | |__| / . \\ \n" +
                " |_|  \\_\\_____/|____/ \\____/_/ \\_\\\n" +
                "                                  \n" +
                "                                  ")

        println("========== SETUP WIZARD ==========")
        println("Would you like to run the setup wizard? (y,n): ")
        if(scanner.hasNext() && scanner.nextLine() == "y") {
            Setup.init()
        } else {
            return
        }

        println("Would you like to download the cache from the RSBox repo? (y,n): ")
        if(scanner.hasNext() && scanner.nextLine() == "y") {
            Setup.downloadCache()
        }

        println("Would you like to generate a RSA encryption key? (y,n): ")
        if(scanner.hasNext() && scanner.nextLine() == "y") {
            Setup.generateRSA(true)
        } else {
            return
        }

        println("======= RSBOX SETUP COMPLETE =======")
        println("You may now start your server. Make sure you import the /rsbox/data/rsa/modulus.txt into your OSRS client.")
    }
}