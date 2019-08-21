package io.rsbox.api.entity

import io.rsbox.api.inter.DisplayMode
import io.rsbox.api.inter.InterfaceDestination
import io.rsbox.api.world.World

/**
 * @author Kyle Escobar
 */

interface Player : LivingEntity {

    var username: String

    var passwordHash: String

    var displayName: String

    var privilege: Int

    var initiated: Boolean

    val world: World

    /**
     * ********** INTERFACES **********
     */

    val displayMode: DisplayMode

    fun openOverlayInterface(displayMode: DisplayMode)

    fun openInterface(parent: Int, child: Int, interfaceId: Int, type: Int = 0, isModal: Boolean = false)

    fun openInterface(interfaceId: Int, dest: InterfaceDestination, fullscreen: Boolean = false)

    fun openInterface(dest: InterfaceDestination, autoClose: Boolean = false)
}