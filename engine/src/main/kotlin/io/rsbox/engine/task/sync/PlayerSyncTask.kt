package io.rsbox.engine.task.sync

import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.net.packet.model.GamePacketBuilder
import io.rsbox.engine.net.packet.model.PacketType
import io.rsbox.engine.task.sync.segment.*
import mu.KLogging

/**
 * @author Kyle Escobar
 */

object PlayerSyncTask : SyncTask<Player>, KLogging() {

    private const val MAX_LOCAL_PLAYERS = 255

    private const val MAX_NEW_PLAYERS_PER_TICK = 40

    override fun run(entity: Player) {
        val player = entity

        val packetBuf = GamePacketBuilder(79, PacketType.VARIABLE_SHORT)
        val maskBuf = GamePacketBuilder()

        val segments = getSegments(player)
        segments.forEach { segment ->
            segment.encode(if(segment is PlayerUpdateBlockSegment) maskBuf else packetBuf)
        }

        packetBuf.putBytes(maskBuf.byteBuf)
        entity.sendPacket(packetBuf.toGamePacket())

        player.gpiLocalCount = 0
        player.gpiExternalCount = 0
        for(i in 1 until 2048) {
            if(player.gpiLocalPlayers[i] != null) {
                player.gpiLocalIndexes[player.gpiLocalCount++] = i
            } else {
                player.gpiExternalIndexes [player.gpiExternalCount++] = 1
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

            val needsBlockUpdate = local!!.blockBuffer.isDirty()

            if(needsBlockUpdate) {
                segments.add(PlayerUpdateBlockSegment(local, false))

                segments.add(SignalPlayerUpdateBlockSegment())
            }

            for(j in i + 1 until player.gpiLocalCount) {
                val nextIndex = player.gpiLocalIndexes[j]
                val next = player.gpiLocalPlayers[nextIndex]
                val skipNext = when(initial) {
                    true -> (player.gpiInactivityFlags[nextIndex] and 0x1) != 0
                    else -> (player.gpiInactivityFlags[nextIndex] and 0x1) == 0
                }

                if(skipNext) continue

                if(next == null || next.blockBuffer.isDirty() || next != player && shouldRemove(player, next)) {
                    break
                }
                skipCount++
            }
            segments.add(PlayerSkipCountSegment(skipCount))
            player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
        }
    }

    private fun addExternalSegments(player: Player, initial: Boolean, previouslyAdded: Int, segments: MutableList<SyncSegment>): Int {
        var skipCount = 0
        var added = previouslyAdded

        for(i in 0 until player.gpiExternalCount) {
            val index = player.gpiExternalIndexes[i]
            val skip = when(initial) {
                true -> (player.gpiInactivityFlags[index] and 0x1) == 0
                else -> (player.gpiInactivityFlags[index] and 0x1) != 0
            }

            if(skip) continue

            if(skipCount > 0) {
                skipCount--
                player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
                continue
            }

            val nonLocal = if(index < player._world.players.capacity) player._world.players[index] else null

            for(j in i + 1 until player.gpiExternalCount) {
                val nextIndex = player.gpiExternalIndexes[j]
                val skipNext = when(initial) {
                    true -> (player.gpiInactivityFlags[nextIndex] and 0x1) == 0
                    else -> (player.gpiInactivityFlags[nextIndex] and 0x1) != 0
                }

                if(skipNext) continue

                val next = if(nextIndex < player._world.players.capacity) player._world.players[nextIndex] else null
                if(next != null && (shouldAdd(player, next) || next._tile.asTileHashMultiplier != player.gpiTileHashMultipliers[nextIndex])) {
                    break
                }
                skipCount++
            }
            // TODO add player skip count
            player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
        }
        return added
    }

    private fun shouldAdd(player: Player, other: Player): Boolean = other._tile.isWithinRadius(player._tile, Player.NORMAL_VIEW_DISTANCE) && other != player

    private fun shouldRemove(player: Player, other: Player): Boolean = !other._tile.isWithinRadius(player._tile, Player.NORMAL_VIEW_DISTANCE)
}