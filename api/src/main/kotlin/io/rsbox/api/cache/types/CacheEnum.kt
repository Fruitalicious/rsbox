package io.rsbox.api.cache.types

import io.rsbox.api.cache.CacheDef

/**
 * The cache enums loaded from the game cache.
 *
 * @author Kyle Escobar
 */

interface CacheEnum : CacheDef {

    /**
     * An integer representing the enum type
     */
    var keyType: Int

    /**
     * An integer representing the enum value type
     */
    var valueType: Int

    /**
     * If no data is found for getInt(), this value will be returned.
     */
    var defaultInt: Int

    /**
     * If no data is found for getString(), this value will be returned.
     */
    var defaultString: String

    /**
     * Gets the int value of a given key.
     * @return Integer
     */
    fun getInt(key: Int): Int

    /**
     * Gets the string value of a given key.
     * @return String
     */
    fun getString(key: Int): String
}