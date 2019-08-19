package io.rsbox.net.codec.login

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.rsbox.engine.Engine
import io.rsbox.engine.config.ServerConfig
import io.rsbox.engine.config.spec.ServerSpec
import io.rsbox.engine.crypt.rsa.RSA
import io.rsbox.engine.net.login.LoginRequest
import io.rsbox.engine.ServerResponse
import io.rsbox.net.codec.StatefulMessageDecoder
import mu.KLogging
import java.math.BigInteger
import io.rsbox.util.BufferUtils.readString
import io.rsbox.util.BufferUtils.readJagexString
import io.rsbox.util.Xtea

/**
 * Handles decoding data from a channel and creating login request.
 * Once this cass creates a [LoginRequest] the [LoginContext] class will
 * handle the processing.
 *
 * @author Kyle Escobar
 */

class LoginDecoder(private val seed: Long) : StatefulMessageDecoder<LoginDecoderState>(LoginDecoderState.LOGIN_HANDSHAKE) {

    private var reconnecting = false

    private var payloadLength = -1

    /**
     * Our initial class that gets called when new channel data arrives.
     * It by default will first run the decodeHandshake() method.
     */
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>, state: LoginDecoderState) {
        buf.markReaderIndex()
        when(state) {
            LoginDecoderState.LOGIN_HANDSHAKE -> decodeHandshake(ctx, buf)
            LoginDecoderState.LOGIN_HEADER -> decodeHeader(ctx, buf, out)
        }
    }

    /**
     * Checks to see if the opcode is 16 or 18. This indicates whether or not
     * the client is reconnecting.
     * After this the state will process the header.
     */
    private fun decodeHandshake(ctx: ChannelHandlerContext, buf: ByteBuf) {
        if(!buf.isReadable) return

        val opcode = buf.readByte().toInt()
        if(opcode == OPCODE_LOGIN || opcode == OPCODE_RECONNECT) {
            reconnecting = opcode == OPCODE_RECONNECT
            setState(LoginDecoderState.LOGIN_HEADER)
        } else {
            ctx.sendServerResponse(ServerResponse.BAD_SESSION_ID)
        }
    }

    private fun decodeHeader(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if(buf.readableBytes() >= 3) {
            val size = buf.readUnsignedShort()
            if(buf.readableBytes() >= size) {
                val revision = buf.readInt()
                buf.skipBytes(Int.SIZE_BYTES)
                buf.skipBytes(Byte.SIZE_BYTES)
                if(revision == ServerConfig.SERVER[ServerSpec.revision]) {
                    payloadLength = size - (Int.SIZE_BYTES + Int.SIZE_BYTES + Byte.SIZE_BYTES)
                    decodeLoginRequest(ctx, buf, out)
                } else {
                    ctx.sendServerResponse(ServerResponse.REVISION_MISMATCH)
                }
            } else {
                buf.resetReaderIndex()
            }
        }
    }

    private fun decodeLoginRequest(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if(buf.readableBytes() >= payloadLength) {
            buf.markReaderIndex()

            val secureBufLength = buf.readUnsignedShort()
            val secureBufHashes = buf.readBytes(secureBufLength)
            val rsaValue = BigInteger(secureBufHashes.array()).modPow(RSA.exponent, RSA.modulus)
            val secureBuf = Unpooled.wrappedBuffer(rsaValue.toByteArray())

            val secureBufCheck = secureBuf.readUnsignedByte().toInt() == 1
            if(!secureBufCheck) {
                buf.resetReaderIndex()
                buf.skipBytes(payloadLength)
                logger.info("Client was disconnected due to invalid encryption data. Channel = ${ctx.channel()}")
                ctx.sendServerResponse(ServerResponse.COULD_NOT_COMPLETE_LOGIN)
                return
            }

            val xteaKeys = IntArray(4) { secureBuf.readInt() }
            val reportedSeed = secureBuf.readLong()

            val authCode: Int
            val password: String?
            val previousXteaKeys = IntArray(4)

            if(reconnecting) {
                for(i in 0 until previousXteaKeys.size) {
                    previousXteaKeys[i] = secureBuf.readInt()
                }

                authCode = -1
                password = null
            } else {
                val authType = secureBuf.readByte().toInt()
                when(authType) {
                    1 -> {
                        authCode = secureBuf.readInt()
                    }

                    0, 2 -> {
                        authCode = secureBuf.readUnsignedMedium()
                        secureBuf.skipBytes(Byte.SIZE_BYTES)
                    }

                    else -> {
                        authCode = secureBuf.readInt()
                    }
                }

                secureBuf.skipBytes(Byte.SIZE_BYTES)
                password = secureBuf.readString()
            }

            val xteaBuf = buf.decryptXteaBuffer(xteaKeys)

            val username = xteaBuf.readString()

            if(reportedSeed != seed) {
                xteaBuf.resetReaderIndex()
                xteaBuf.skipBytes(payloadLength)
                logger.info("Login request reject for username={} because of a seed mismatch.", username)
                ctx.sendServerResponse(ServerResponse.COULD_NOT_COMPLETE_LOGIN)
                return
            }

            val clientResizable = (xteaBuf.readByte().toInt() shr 1) == 1
            val clientWidth = xteaBuf.readUnsignedShort()
            val clientHeight = xteaBuf.readUnsignedShort()

            xteaBuf.skipBytes(24) // random.dat data
            xteaBuf.readString()
            xteaBuf.skipBytes(Int.SIZE_BYTES)

            xteaBuf.skipBytes(Byte.SIZE_BYTES * 10)
            xteaBuf.skipBytes(Short.SIZE_BYTES)
            xteaBuf.skipBytes(Byte.SIZE_BYTES)
            xteaBuf.skipBytes(Byte.SIZE_BYTES * 3)
            xteaBuf.skipBytes(Short.SIZE_BYTES)
            xteaBuf.readJagexString()
            xteaBuf.readJagexString()
            xteaBuf.readJagexString()
            xteaBuf.readJagexString()
            xteaBuf.skipBytes(Byte.SIZE_BYTES)
            xteaBuf.skipBytes(Short.SIZE_BYTES)
            xteaBuf.readJagexString()
            xteaBuf.readJagexString()
            xteaBuf.skipBytes(Byte.SIZE_BYTES * 2)
            xteaBuf.skipBytes(Int.SIZE_BYTES * 3)
            xteaBuf.skipBytes(Int.SIZE_BYTES)
            xteaBuf.readJagexString()

            xteaBuf.skipBytes(Int.SIZE_BYTES * 3)

            val cacheCrcs = Engine.cacheStore.indexes.map { it.crc }.toIntArray()
            val crcs = IntArray(cacheCrcs.size) { xteaBuf.readInt() }

            for(i in 0 until crcs.size) {
                if(i == 16) continue

                if(crcs[i] != cacheCrcs[i]) {
                    buf.resetReaderIndex()
                    buf.skipBytes(payloadLength)
                    logger.info("Login request for {} was rejected due to cache hash mismatch.", username)
                    ctx.sendServerResponse(ServerResponse.REVISION_MISMATCH)
                    return
                }
            }

            val request = LoginRequest(
                channel = ctx.channel(),
                username = username,
                password = password ?: "",
                revision = ServerConfig.SERVER[ServerSpec.revision],
                xteaKeys = xteaKeys,
                authCode = authCode,
                uuid = "".toUpperCase(),
                reconnecting = reconnecting,
                resizableClient = clientResizable,
                clientWidth = clientWidth,
                clientHeight = clientHeight
            )

            out.add(request)
        }
    }

    private fun ChannelHandlerContext.sendServerResponse(response: ServerResponse) {
        val buf = channel().alloc().buffer(1)
        buf.writeByte(response.id)
        writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE)
    }

    private fun ByteBuf.decryptXteaBuffer(xteaKeys: IntArray): ByteBuf {
        val data = ByteArray(readableBytes())
        readBytes(data)
        return Unpooled.wrappedBuffer(Xtea.decipher(xteaKeys, data, 0, data.size))
    }

    companion object : KLogging() {
        private const val OPCODE_LOGIN = 16
        private const val OPCODE_RECONNECT = 18
    }
}