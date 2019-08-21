package io.rsbox.engine.task.sync

import io.rsbox.engine.model.entity.LivingEntity

/**
 * @author Kyle Escobar
 */

interface SyncTask<T : LivingEntity> {
    fun run(entity: T)
}