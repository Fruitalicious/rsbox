package io.rsbox.engine.config.spec

import com.uchuhimo.konf.ConfigSpec

/**
 * @author Kyle Escobar
 */

object ServerSpec : ConfigSpec("server") {
    val name by optional("RSBox Server", "name")
    val revision by optional(181, "revision")
    val auto_register by optional(true, "auto_register")

    // Network
    val net_address by optional("0.0.0.0", "network.address")
    val net_port by optional(43594, "network.port")
    val net_threads by optional(2, "network.threads")

    // Encryption
    val rsa_bits by optional(2048, "encryption.rsa.bits")
}