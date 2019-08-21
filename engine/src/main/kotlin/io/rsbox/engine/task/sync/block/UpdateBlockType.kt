package io.rsbox.engine.task.sync.block

/**
 * @author Kyle Escobar
 */

enum class UpdateBlockType(val mask: Int) {
    APPEARANCE(0x1),

    ANIMATION(0x80),

    GFX(0x200),

    CHAT(0x10),

    FACE_TILE(0x4),

    FACE_ENTITY(0x2),

    MOVEMENT(0x1000),

    CONTEXT_MENU(0x100),

    FORCE_MOVEMENT(0x400),

    HITMARK(0x40),

    FORCE_CHAT(0x20);
}