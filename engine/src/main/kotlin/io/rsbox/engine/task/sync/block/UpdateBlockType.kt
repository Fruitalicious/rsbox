package io.rsbox.engine.task.sync.block

/**
 * @author Kyle Escobar
 */

enum class UpdateBlockType(val mask: Int) {
    APPEARANCE(0x1),

    ANIMATION(0x10),

    GFX(0x400),

    CHAT(0x40),

    FACE_TILE(0x2),

    FACE_PAWN(0x8),

    MOVEMENT(0x1000),

    CONTEXT_MENU(0x200),

    FORCE_MOVEMENT(0x100),

    HITMARK(0x80),

    FORCE_CHAT(0x4);
}