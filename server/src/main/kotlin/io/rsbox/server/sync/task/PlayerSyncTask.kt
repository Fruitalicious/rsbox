package io.rsbox.server.sync.task

import io.rsbox.server.model.entity.Player
import io.rsbox.server.model.world.World
import io.rsbox.server.net.packet.builder.GamePacketBuilder
import io.rsbox.server.net.packet.builder.PacketType
import io.rsbox.server.sync.SyncSegment
import io.rsbox.server.sync.SyncTask
import io.rsbox.server.sync.segment.*

/**
 * @author Kyle Escobar
 *
 * Credit goes to Tomm. Used in his RSMod project.
 */

object PlayerSyncTask : SyncTask<Player> {

    private const val MAX_LOCAL_PLAYERS = 255
    private const val MAX_NEW_PLAYER_RENDERS_PER_CYCLE = 40

    override fun run(entity: Player) {
        val player = entity

        val buf = GamePacketBuilder((player.world as World).playerUpdateBlocks.updateOpcode, PacketType.VARIABLE_SHORT)
        val maskBuf = GamePacketBuilder()

        val segments = getSegments(player)
        segments.forEach { segment ->
            segment.encode(if(segment is PlayerUpdateSegment) maskBuf else buf)
        }

        buf.putBytes(maskBuf.byteBuf)
        player.write(buf.toGamePacket())

        player.gpiLocalCount = 0
        player.gpiExternalCount = 0
        for(i in 1 until 2048) {
            if(player.gpiLocalPlayers[i] != null) {
                player.gpiLocalIndexes[player.gpiLocalCount++] = i
            } else {
                player.gpiExternalIndexes[player.gpiExternalCount++] = i
            }
            player.gpiInactivityFlags[i] = player.gpiInactivityFlags[i] shr 1
        }
    }

    private fun getSegments(player: Player): List<SyncSegment> {
        val segments = mutableListOf<SyncSegment>()

        segments.add(SetBitAccessSegment())
        addLocalSegments(player, true, segments)
        segments.add(SetByteAccessSegment())

        segments.add(SetBitAccessSegment())
        addLocalSegments(player, false, segments)
        segments.add(SetByteAccessSegment())

        var added = 0

        segments.add(SetBitAccessSegment())
        added += addExternalSegments(player, true, added, segments)
        segments.add(SetByteAccessSegment())

        segments.add(SetBitAccessSegment())
        added += addExternalSegments(player, false, added, segments)
        segments.add(SetByteAccessSegment())

        return segments
    }

    private fun addLocalSegments(player: Player, initial: Boolean, segments: MutableList<SyncSegment>) {
        var skipCount = 0

        for(i in 0 until player.gpiLocalCount) {
            val index = player.gpiLocalIndexes[i]
            val local = player.gpiLocalPlayers[index]

            val skip = when(initial) {
                true -> (player.gpiInactivityFlags[index] and 0x1) != 0
                else -> (player.gpiInactivityFlags[index] and 0x1) == 0
            }

            if(skip) continue

            if(skipCount > 0) {
                skipCount--
                player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
                continue
            }

            if(local != player && (local == null)) {
                // TODO Create a way not to render players too far away.
                val lastTileHash = player.gpiTileHashMultipliers[index]
                val currentTileHash = local?.tile?.asTileHashMultiplier ?: 0
                val updateTileHash = lastTileHash != currentTileHash

                segments.add(RemoveLocalPlayerSegment(updateTileHash))
                if(updateTileHash) {
                    segments.add(PlayerLocationHashSegment(lastTileHash, currentTileHash))
                }

                player.gpiLocalPlayers[index] = null
                player.gpiTileHashMultipliers[index] = currentTileHash

                continue
            }

            val doBlockUpdate = local.blockBuffer.hasChanged()
            if(doBlockUpdate) {
                segments.add(PlayerUpdateSegment(local, false))
            }
        }
    }

    private fun addExternalSegments(player: Player, initial: Boolean, previouslyAdded: Int, segments: MutableList<SyncSegment>): Int {
        return 0
    }
}