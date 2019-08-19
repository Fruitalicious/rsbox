package io.rsbox.net

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import io.rsbox.net.codec.handshake.HandshakeDecoder
import io.rsbox.net.codec.handshake.HandshakeEncoder
import io.rsbox.net.context.ContextHandler

/**
 * @author Kyle Escobar
 */

class PipelineFactory : ChannelInitializer<SocketChannel>() {

    /**
     * Traffic throttler bottlenecks traffic that channels can take up.
     * You can increase this limit for production but I would not remove it.
     * Removing this makes your whole host running the server susceptible to ddos attacks.
     *
     * @default Unlimited bandwidth
     */
    //private val globalTrafficBandwidth = GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000)

    /**
     * Channel traffic throttler does the same thing as the global except instead of the whole system,
     * It limits the bandwidth a single connection can use.
     *
     * @default Unlimited ingress per second / connection
     * @default 1MB egress per second / connection
     */
    //private val channelTrafficBandwidth = ChannelTrafficShapingHandler(0, 1024 * 1024, 1000)

    private val handler = ContextHandler()

    override fun initChannel(ch: SocketChannel) {
        val p = ch.pipeline()

        p.addLast("timeout", IdleStateHandler(30, 0, 0))
        p.addLast("handshake_encoder", HandshakeEncoder())
        p.addLast("handshake_decoder", HandshakeDecoder())
        p.addLast("handler", handler)
    }
}