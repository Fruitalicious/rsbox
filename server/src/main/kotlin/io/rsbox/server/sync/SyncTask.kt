package io.rsbox.server.sync

import io.rsbox.server.model.entity.LivingEntity

/**
 * @author Kyle Escobar
 */

interface SyncTask<T : LivingEntity> {
    fun run(entity: T)
}