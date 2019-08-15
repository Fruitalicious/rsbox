package io.rsbox.api.entity

import io.rsbox.api.Server
import io.rsbox.api.inter.DisplayMode
import io.rsbox.api.inter.InterfaceDestination
import io.rsbox.api.inter.InterfaceSet
import io.rsbox.api.world.World

/**
 * @author Kyle Escobar
 */

interface Player : LivingEntity {
    val username: String

    val displayName: String

    val password: String

    val uuid: String

    val privilege: Int

    val interfaces: InterfaceSet



    fun openOverlayInterface(displayMode: DisplayMode)

    fun getInterfaceAt(dest: InterfaceDestination): Int

    fun openInterface(dest: InterfaceDestination, autoClose: Boolean)

    fun openInterface(parent: Int, child: Int, interfaceId: Int, type: Int, isModal: Boolean)

    fun openInterface(interfaceId: Int, dest: InterfaceDestination, fullscreen: Boolean = false)



    fun getVarbit(id: Int): Int

    fun getVarp(id: Int): Int

    fun setVarbit(id: Int, value: Int)

    fun setVarp(id: Int, value: Int)

    fun syncVarp(id: Int)



    fun runClientScript(id: Int, vararg args: Any)
}