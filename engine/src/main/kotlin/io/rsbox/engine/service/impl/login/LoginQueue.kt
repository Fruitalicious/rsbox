package io.rsbox.engine.service.impl.login

import com.google.common.base.Stopwatch
import io.netty.channel.ChannelFutureListener
import io.rsbox.engine.EngineConstants
import io.rsbox.engine.ServerResponse
import io.rsbox.engine.config.ServerConfig
import io.rsbox.engine.config.spec.ServerSpec
import io.rsbox.engine.net.login.LoginRequest
import io.rsbox.engine.system.serializer.player.PlayerLoader
import io.rsbox.util.IsaacRandom
import mu.KLogging
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author Kyle Escobar
 */

class LoginQueue(private val service: LoginService) : Runnable {

    override fun run() {
        await()
    }

    private fun await() {
        while(service.started) {
            val request = service.loginRequestQueue.take()
            val stopwatch = Stopwatch.createStarted()
            val status = process(request)
            when(status) {
                ServerResponse.ACCEPTABLE -> {
                    val player = PlayerLoader.loadPlayerFromRequest(request)
                    stopwatch.stop()
                    logger.info("Login for user {} completed successfully in {}ms. Logging player into the game.", request.username, stopwatch.elapsed(TimeUnit.MILLISECONDS))

                    val decodeRandom = IsaacRandom(request.xteaKeys)
                    val encodeRandom = IsaacRandom(IntArray(request.xteaKeys.size){request.xteaKeys[it] + 50})
                    service.executeLogin(player, encodeRandom, decodeRandom)
                }

                else -> {
                    request.channel.writeAndFlush(status).addListener(ChannelFutureListener.CLOSE)
                    logger.info("Login request for user {} denied with status {}.", request.username, status)
                }
            }
        }
    }

    private fun process(request: LoginRequest): ServerResponse {
        // Check if player save exists.
        val file = File("${EngineConstants.PLAYER_SAVES_PATH}${request.username}.yml")
        if(!file.exists()) {
            if(ServerConfig.SERVER[ServerSpec.auto_register]) {
                PlayerLoader.createPlayerSave(request.username, request.password)
                logger.info("Created new player save for username=${request.username}.")
            } else {
                return ServerResponse.INVALID_CREDENTIALS
            }
        }

        // Check password
        if(!PlayerLoader.checkPassword(request.username, request.password)) {
            return ServerResponse.INVALID_CREDENTIALS
        }

        return ServerResponse.ACCEPTABLE
    }

    companion object : KLogging()
}