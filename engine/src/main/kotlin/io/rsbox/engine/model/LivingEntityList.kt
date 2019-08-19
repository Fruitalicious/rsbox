package io.rsbox.engine.model

import io.rsbox.engine.model.entity.LivingEntity

/**
 * @author Kyle Escobar
 */

class LivingEntityList<T : LivingEntity>(private val entities: Array<T?>) {

    val entries: Array<T?> = entities

    val capacity = entities.size

    private var count = 0

    operator fun get(index: Int): T? = entities[index]

    fun contains(entity: T): Boolean = entities[entity.index] == entity

    fun count(): Int = count

    fun count(predicate: (T) -> Boolean): Int {
        var count = 0
        for(element in entities) {
            if(element != null && predicate(element)) {
                count++
            }
        }
        return count
    }

    fun add(entity: T): Boolean {
        for(i in 1 until entities.size) {
            if(entities[i] == null) {
                entities[i] = entity
                entity.index = i
                count++
                return true
            }
        }
        return false
    }

    fun remove(entity: T): Boolean {
        if(entities[entity.index] == entity) {
            entities[entity.index] = null
            entity.index = -1
            count--
            return true
        }
        return false
    }

    fun remove(index: Int): T? {
        if(entities[index] != null) {
            val entity = entities[index]
            entities[index] = null
            count--
            return entity
        }
        return null
    }

    fun firstOrNull(predicate: (T) -> Boolean): T? {
        for(element in entities) {
            if(element != null && predicate(element)) {
                return element
            }
        }
        return null
    }

    fun forEach(action: (T) -> Unit) {
        for(element in entities) {
            if(element != null) {
                action(element)
            }
        }
    }
}