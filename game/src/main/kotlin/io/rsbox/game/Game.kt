package io.rsbox.game

import io.rsbox.api.entity.Player
import io.rsbox.api.inter.InterfaceDestination

/**
 * @author Kyle Escobar
 */

object Game {

    fun setupRootInterfaces(player: Player) {
        player.openOverlayInterface(player.displayMode)

        InterfaceDestination.values.filter { pane -> pane.interfaceId != -1 }.forEach { pane ->
            player.openInterface(pane.interfaceId, pane)
        }
    }

}