package io.rsbox.engine.model

/**
 * @author Kyle Escobar
 */

data class ChatMessage(val text: String, val icon: Int, val type: ChatType, val effect: ChatEffect, val color: ChatColor) {

    enum class ChatType(val id: Int) {
        NONE(0),
        AUTOCHAT(1),
        CLANCHAT(2);
    }

    enum class ChatEffect(val id: Int) {
        NONE(0),
        WAVE(1),
        WAVE2(2),
        SHAKE(3),
        SCROLL(4),
        SLIDE(5);
    }

    enum class ChatColor(val id: Int) {
        NONE(0),
        RED(1),
        GREEN(2),
        CYAN(3),
        PURPLE(4),
        WHITE(5),
        FLASH1(6),
        FLASH2(7),
        FLASH3(8),
        GLOW1(9),
        GLOW2(10),
        GLOW3(11);
    }
}