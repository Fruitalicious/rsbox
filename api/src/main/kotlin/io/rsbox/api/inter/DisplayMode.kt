package io.rsbox.api.inter

/**
 * @author Kyle Escobar
 */

enum class DisplayMode(val id: Int) {

    FIXED(id = 0),

    RESIZABLE_NORMAL(id = 1),

    RESIZABLE_LIST(id = 2),

    MOBILE(id = 3),

    FULLSCREEN(id = 4);

    fun isResizable(): Boolean = this == RESIZABLE_NORMAL || this == RESIZABLE_LIST

    companion object {
        val values = enumValues<DisplayMode>()
    }
}