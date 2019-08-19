package io.rsbox.engine.system.serializer.player.spec

import com.uchuhimo.konf.ConfigSpec

/**
 * @author Kyle Escobar
 */

object PlayerSpec : ConfigSpec("player") {
    val username by required<String>("username")
    val password by required<String>("password")
    val displayName by required<String>("displayName")
    val privilege by required<Int>("privilege")
}