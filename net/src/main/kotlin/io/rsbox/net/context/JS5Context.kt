package io.rsbox.net.context

import com.google.common.primitives.Ints
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.rsbox.engine.Engine
import io.rsbox.net.codec.js5.JS5Request
import io.rsbox.net.codec.js5.JS5Response
import mu.KLogging
import net.runelite.cache.fs.Container
import net.runelite.cache.fs.jagex.CompressionType
import net.runelite.cache.fs.jagex.DiskStorage

/**
 * @author Kyle Escobar
 */

class JS5Context(channel: Channel) : ServerContext(channel) {

    override fun receiveMessage(ctx: ChannelHandlerContext, msg: Any) {
        if(msg is JS5Request) {
            if(msg.index == 255) {
                encodeIndexData(ctx, msg)
            } else {
                encodeFileData(ctx, msg)
            }
        }
    }

    override fun terminate() {

    }

    private fun encodeIndexData(ctx: ChannelHandlerContext, msg: JS5Request) {
        val data: ByteArray
        if(msg.archive == 255) {
            if(cachedIndexData == null) {
                val buf = ctx.alloc().heapBuffer(Engine.cacheStore.indexes.size * 8)

                Engine.cacheStore.indexes.forEach { index ->
                    buf.writeInt(index.crc)
                    buf.writeInt(index.revision)
                }

                val container = Container(CompressionType.NONE, -1)
                container.compress(buf.array().copyOf(buf.readableBytes()), null)
                cachedIndexData = container.data
                buf.release()
            }
            data = cachedIndexData!!
        } else {
            val storage = Engine.cacheStore.storage as DiskStorage
            data = storage.readIndex(msg.archive)
        }

        val response = JS5Response(msg.index, msg.archive, data)
        ctx.writeAndFlush(response)
    }

    private fun encodeFileData(ctx: ChannelHandlerContext, msg: JS5Request) {
        val index = Engine.cacheStore.findIndex(msg.index)!!
        val archive = index.getArchive(msg.archive)!!
        var data = Engine.cacheStore.storage.loadArchive(archive)

        if(data != null) {
            val compression = data[0]
            val length = Ints.fromBytes(data[1], data[2], data[3], data[4])
            val expectedLength = length + (if (compression.toInt() != CompressionType.NONE) 9 else 5)
            if(expectedLength != length && data.size - expectedLength == 2) {
                data = data.copyOf(data.size - 2)
            }

            val response = JS5Response(msg.index, msg.archive, data)
            ctx.writeAndFlush(response)
        } else {
            logger.warn("Cache data could not be read from archive. index={} archive={}", msg.index, msg.archive)
        }
    }

    companion object : KLogging() {
        private var cachedIndexData: ByteArray? = null
    }
}