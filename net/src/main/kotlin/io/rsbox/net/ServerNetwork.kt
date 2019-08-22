package io.rsbox.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.rsbox.engine.config.ServerConfig
import io.rsbox.engine.config.spec.ServerSpec
import mu.KLogging
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * @author Kyle Escobar
 */

object ServerNetwork : KLogging() {

    private val acceptGroup = NioEventLoopGroup(ServerConfig.SERVER[ServerSpec.net_threads])
    private val ioGroup = NioEventLoopGroup(1)

    val bootstrap = ServerBootstrap()

    fun start() {
        bootstrap.group(acceptGroup, ioGroup)
        bootstrap.channel(NioServerSocketChannel::class.java)
        bootstrap.childHandler(PipelineFactory())
        bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)

        val addr = ServerConfig.SERVER[ServerSpec.net_address]
        val port = ServerConfig.SERVER[ServerSpec.net_port]

        bootstrap.bind(InetSocketAddress(InetAddress.getByName(addr), port)).sync().awaitUninterruptibly()
        System.gc()

        logger.info("Server network listening on $addr:$port")
    }
}