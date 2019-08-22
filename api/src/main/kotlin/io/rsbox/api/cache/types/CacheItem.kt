package io.rsbox.api.cache.types

import io.rsbox.api.cache.CacheDef

/**
 * The item information from the game cache.
 *
 * @author Kyle Escobar
 */

interface CacheItem : CacheDef {

    /**
     * The name of the item in plain text.
     */
    var name: String

    /**
     * Whether the item stacks or not.
     */
    var canStack: Boolean

    /**
     * The in game calculated cost of the item.
     */
    var cost: Int

    /**
     * Is this a members item?
     */
    var isMembers: Boolean

    /**
     * The menu options available for item when on the ground.
     */
    val groundMenu: Array<String?>

    /**
     * The menu options available for item when in a player's inventory.
     */
    val inventoryMenu: Array<String?>

    /**
     * The menu options available for item when in the equipment tab.
     */
    val equipmentMenu: Array<String?>

    /**
     * Whether or not the item can be traded.
     */
    var isTradable: Boolean

    /**
     * If the item is a team cape, returns the number.
     * If not, returns 0.
     */
    var teamCape: Int

    /**
     * The item id of the current item's noted form.
     * If the item already is noted, returns 0.
     */
    var noteId: Int

    var noteTemplateId: Int

    var placeholderId: Int

    var placeholderTemplateId: Int
}