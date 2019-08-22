package io.rsbox.api.cache

/**
 * This holds all the data loaded from cache during engine start.
 * This class gives the api the ability to load things such as,
 * Item names, npc names, item stats, object names and ids. etc.
 *
 * @author Kyle Escobar
 */
interface CacheData {
    fun <T : CacheDef> get(type: Class<out T>, id: Int): T
}