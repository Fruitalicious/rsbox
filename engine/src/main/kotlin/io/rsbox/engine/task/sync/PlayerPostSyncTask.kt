package io.rsbox.engine.task.sync

import io.rsbox.engine.model.entity.Player

/**
 * @author Kyle Escobar
 */

object PlayerPostSyncTask : SyncTask<Player> {
    override fun run(entity: Player) {
        entity.blockBuffer.clean()
        entity.channel.flush()
    }
}