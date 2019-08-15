package io.rsbox.server.sync.task

import io.rsbox.server.model.entity.Player
import io.rsbox.server.sync.SyncTask

/**
 * @author Kyle Escobar
 */

object PlayerPreSyncTask : SyncTask<Player> {
    override fun run(entity: Player) {

    }
}