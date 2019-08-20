package io.rsbox.server.setup

import com.uchuhimo.konf.Config
import io.rsbox.engine.EngineConstants
import io.rsbox.engine.config.spec.ServerSpec
import io.rsbox.engine.system.crypt.rsa.RSA
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.util.zip.ZipFile

/**
 * @author Kyle Escobar
 */

object Setup {

    val dirs = arrayOf(
        "rsbox/",
        "rsbox/config/",
        "rsbox/data/",
        "rsbox/data/cache/",
        "rsbox/data/xteas/",
        "rsbox/data/defs/",
        "rsbox/data/rsa/",
        "rsbox/data/def/",
        "rsbox/plugins/",
        "rsbox/data/saves/"
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val step = args[0]

        when(step) {
            "init" -> init()
            "cache" -> downloadCache()
            "rsa" -> generateRSA()
        }
    }

    private fun init() {
        if(!checkDirs()) createDirs()
    }

    private fun checkDirs(): Boolean {
        dirs.forEach { dir ->
            val f = File("../$dir")
            if(!f.exists()) return false
        }
        return true
    }

    private fun createDirs() {
        dirs.forEach { dir ->
            val f = File("../$dir")
            if(!f.exists()) {
                f.mkdirs()
                println("Created default directory $dir.")
            }
        }
    }

    private fun downloadCache() {
        println("Downloading Cache...")
        val channel = Channels.newChannel(URL(EngineConstants.CACHE_REPO.replace("<>", Config{addSpec(ServerSpec)}[ServerSpec.revision].toString())).openStream())
        val output = FileOutputStream("../${EngineConstants.CACHE_PATH}cache.zip")
        val fileChannel = output.channel
        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE)
        println("Download complete.")

        this.downloadXteas()
    }

    private fun downloadXteas() {
        println("Downloading Xteas...")
        val channel = Channels.newChannel(URL(EngineConstants.XTEAS_REPO.replace("<>", Config{addSpec(ServerSpec)}[ServerSpec.revision].toString())).openStream())
        val output = FileOutputStream("../${EngineConstants.XTEAS_FILE}")
        output.channel.transferFrom(channel, 0, Long.MAX_VALUE)
        println("Download complete.")

        this.extractCache()
    }

    private fun extractCache() {
        println("Decompressing cache files...")
        val file = File("../${EngineConstants.CACHE_PATH}cache.zip")
        ZipFile(file).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    File("../${EngineConstants.CACHE_PATH}${entry.name}").outputStream().use { output ->
                        input.copyTo(output)
                        println("Decompressed file ${entry.name}")
                        output.close()
                    }
                    input.close()
                }
            }
            zip.close()
        }

        File("../${EngineConstants.CACHE_PATH}cache.zip").delete()

        println("======= DOWNLOAD COMPLETE =======")
    }

    private fun generateRSA() {
        println("Generating RSA key pair...")

        RSA.generate()

        println("Private / Public key pair have been saved in ${EngineConstants.RSA_PRIVATE_FILE.replace("private.pem","")}.")
        println("Please paste the contents of ${EngineConstants.RSA_MODULUS_FILE} into your OSRS client's RSA modulus field.")
    }
}