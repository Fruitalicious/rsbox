package io.rsbox.engine.task.sync

import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.net.packet.model.GamePacketBuilder
import io.rsbox.engine.net.packet.model.PacketType
import io.rsbox.engine.task.sync.segment.*
import mu.KLogging
import java.lang.RuntimeException

/**
 * @author Kyle Escobar
 */

object PlayerSyncTask : SyncTask<Player>, KLogging() {

    private const val MAX_LOCAL_PLAYERS = 255

    private const val MAX_NEW_PLAYERS_PER_TICK = 50

    override fun run(entity: Player) {
        val player = entity

        val packetBuf = GamePacketBuilder(79, PacketType.VARIABLE_SHORT)
        val maskBuf = GamePacketBuilder()

        val segments = getSegments(player)
        for(segment in segments) {
            val targetBuffer = when(segment) {
                is PlayerUpdateBlockSegment -> maskBuf
                else -> packetBuf
            }
            segment.encode(targetBuffer)
        }

        packetBuf.putBytes(maskBuf.byteBuf)
        player.sendPacket(packetBuf.toGamePacket())

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

            if(skip) {
                continue
            }

            if(skipCount > 0) {
                skipCount--
                player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
                continue
            }

            if(local != player && (local == null || shouldRemove(player, local))) {
                val lastTileHash = player.gpiTileHashMultipliers[index]
                val currTileHash = local?.tile?.asTileHashMultiplier ?: 0
                val updateTileHash = lastTileHash != currTileHash

                segments.add(RemoveLocalPlayerSegment(updateTileHash))
                if(updateTileHash) {
                    segments.add(PlayerLocationHashSegment(lastTileHash, currTileHash))
                }

                player.gpiLocalPlayers[index] = null
                player.gpiTileHashMultipliers[index] = currTileHash

                continue
            }

            val requiresBlockUpdate = local.blockBuffer.isDirty()
            if(requiresBlockUpdate) {
                segments.add(PlayerUpdateBlockSegment(local, false))
            }
            if(local.moved) {

            } else if(local.steps != null) {

            } else if(requiresBlockUpdate) {
                segments.add(SignalPlayerUpdateBlockSegment())
            } else {
                for(j in i + 1 until player.gpiLocalCount) {
                    val nextIndex = player.gpiLocalIndexes[j]
                    val next = player.gpiLocalPlayers[nextIndex]
                    val skipNext = when(initial) {
                        true -> (player.gpiInactivityFlags[nextIndex] and 0x1) != 0
                        else -> (player.gpiInactivityFlags[nextIndex] and 0x1) == 0
                    }

                    if(skipNext) {
                        continue
                    }

                    if(next == null || next.blockBuffer.isDirty() || next.moved || next.steps != null || next != player && shouldRemove(player, next)) {
                        break
                    }
                    skipCount++
                }
                segments.add(PlayerSkipCountSegment(skipCount))
                player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
            }
        }

        if(skipCount > 0) {
            throw RuntimeException()
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

            if(skip) {
                continue
            }

            if(skipCount > 0) {
                skipCount--
                player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
                continue
            }

            val nonLocal = if (index < player._world.players.capacity) player._world.players[index] else null

            if(nonLocal != null && added < MAX_NEW_PLAYERS_PER_TICK && player.gpiLocalCount + added < MAX_LOCAL_PLAYERS && shouldAdd(player, nonLocal)) {
                val oldTileHash = player.gpiTileHashMultipliers[index]
                val currTileHash = nonLocal._tile.asTileHashMultiplier

                val tileUpdateSegment = if(oldTileHash == currTileHash) null else PlayerLocationHashSegment(oldTileHash, currTileHash)

                segments.add(AddLocalPlayerSegment(nonLocal, tileUpdateSegment))
                segments.add(PlayerUpdateBlockSegment(nonLocal, true))

                player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
                player.gpiTileHashMultipliers[index] = currTileHash
                player.gpiLocalPlayers[index] = nonLocal

                added++
                continue
            }

            for(j in i + 1 until player.gpiExternalCount) {
                val nextIndex = player.gpiExternalIndexes[j]
                val skipNext = when(initial) {
                    true -> (player.gpiInactivityFlags[nextIndex] and 0x1) == 0
                    else -> (player.gpiInactivityFlags[nextIndex] and 0x1) != 0
                }

                if(skipNext) {
                    continue
                }

                val next = if(nextIndex < player._world.players.capacity) player._world.players[nextIndex] else null
                if(next != null && (shouldAdd(player, next) || next._tile.asTileHashMultiplier != player.gpiTileHashMultipliers[nextIndex])) {
                    break
                }

                skipCount++
            }

            segments.add(PlayerSkipCountSegment(skipCount))
            player.gpiInactivityFlags[index] = player.gpiInactivityFlags[index] or 0x2
        }

        if(skipCount > 0) {
            throw RuntimeException()
        }

        return added
    }

    private fun shouldAdd(player: Player, other: Player): Boolean = other._tile.isWithinRadius(player._tile, Player.NORMAL_VIEW_DISTANCE) && other != player
    private fun shouldRemove(player: Player, other: Player): Boolean = !other._tile.isWithinRadius(player._tile, Player.NORMAL_VIEW_DISTANCE)
}