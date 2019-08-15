package io.rsbox.server.sync.block.impl

import io.rsbox.server.net.packet.builder.DataTransformation
import io.rsbox.server.net.packet.builder.DataType
import io.rsbox.server.sync.block.UpdateBlockType
import io.rsbox.server.sync.block.dsl.UpdateBlock
import io.rsbox.server.sync.block.dsl.UpdateType
import io.rsbox.server.sync.block.dsl.builder

/**
 * @author Kyle Escobar
 */

class PlayerUpdateBlock : UpdateBlock(type = UpdateType.PLAYER, opcode = 79, mask = 0x8) {
    override fun build() = builder(this) {
        order {
            entry[0] = UpdateBlockType.HITMARK
            entry[1] = UpdateBlockType.GFX
            entry[2] = UpdateBlockType.MOVEMENT
            entry[3] = UpdateBlockType.FORCE_MOVEMENT
            entry[4] = UpdateBlockType.FORCE_CHAT
            entry[5] = UpdateBlockType.FACE_TILE
            entry[6] = UpdateBlockType.APPEARANCE
            entry[7] = UpdateBlockType.FACE_ENTITY
            entry[8] = UpdateBlockType.PUBLIC_CHAT
            entry[9] = UpdateBlockType.ANIMATION
        }

        blocks {
            block = UpdateBlockType.APPEARANCE
            bit = 0x1

            frame(
                name = "length",
                type = DataType.BYTE,
                trans = DataTransformation.SUBTRACT
            )

            frame(
                name = "buffer",
                type = DataType.BYTES,
                trans = DataTransformation.ADD
            )
        }
    }
}