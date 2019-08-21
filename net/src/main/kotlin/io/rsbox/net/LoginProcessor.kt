package io.rsbox.net

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.rsbox.engine.service.ServiceManager
import io.rsbox.engine.service.impl.login.LoginService
import io.rsbox.net.codec.game.GamePacketDecoder
import io.rsbox.net.codec.game.GamePacketEncoder
import io.rsbox.net.context.ContextHandler
import io.rsbox.engine.net.game.GameContext
import mu.KLogging
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object LoginProcessor : KLogging() {

    private val service = ServiceManager[LoginService::class.java]!!

    private val executor: ExecutorService = Executors.newSingleThreadExecutor(
        ThreadFactoryBuilder()
            .setNameFormat("login-processor")
            .setUncaughtExceptionHandler { t, e -> logger.error("An error occurred in thread $t.", e) }.build()
    )

    fun start() {
        val run = Runnable {
            while(true) {
                val next = service.successfulLoginRequeustQueue.take()
                val gameContext = GameContext(next.player.channel)
                next.player.channel.attr(ContextHandler.CONTEXT_KEY).set(gameContext)

                next.player.context = gameContext

                val p = next.player.channel.pipeline()

                if(next.player.channel.isActive) {
                    p.remove("handshake_encoder")
                    p.remove("login_decoder")
                    p.remove("login_encoder")

                    p.addFirst("packet_encoder", GamePacketEncoder(next.encodeRandom))
                    p.addBefore("handler", "packet_decoder", GamePacketDecoder(next.decodeRandom))

                    next.player.login()
                    next.player.channel.flush()
                }
            }
        }

        executor.submit(run)
        logger.info("Login processor is now running.")
    }
}