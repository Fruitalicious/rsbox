package io.rsbox.engine.service.impl.login

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.net.login.LoginRequest
import io.rsbox.engine.net.login.LoginResponse
import io.rsbox.engine.service.Service
import io.rsbox.util.IsaacRandom
import mu.KLogging
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue

/**
 * @author Kyle Escobar
 */

class LoginService : Service() {

    val loginRequestQueue = LinkedBlockingDeque<LoginRequest>()

    val successfulLoginRequeustQueue = LinkedBlockingQueue<SuccessfulLoginRequest>()

    private val loginThreads = 2

    private val executor = Executors.newFixedThreadPool(loginThreads,
        ThreadFactoryBuilder()
            .setNameFormat("login-queue")
            .setUncaughtExceptionHandler { t, e -> logger.error("Error occurred in thread $t.", e) }.build())

    var started = false

    override fun start() {
        started = true

        for(i in 1..loginThreads) {
            executor.submit(LoginQueue(this))
        }
    }

    override fun stop() {
        started = false
    }

    fun addLoginRequest(request: LoginRequest) {
        loginRequestQueue.offer(request)
        logger.info("Login request received for {} from {}", request.username, request.channel)
    }

    fun executeLogin(player: Player, encodeRandom: IsaacRandom, decodeRandom: IsaacRandom) {
        player.register()
        player.channel.writeAndFlush(
            LoginResponse(
                player = player,
                encodeRandom = encodeRandom,
                decodeRandom = decodeRandom
            )
        )

        successfulLoginRequeustQueue.offer(SuccessfulLoginRequest(player, encodeRandom, decodeRandom))
    }

    companion object : KLogging()
}