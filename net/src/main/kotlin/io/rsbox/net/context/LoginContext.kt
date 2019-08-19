package io.rsbox.net.context

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.rsbox.engine.service.ServiceManager
import io.rsbox.engine.service.impl.login.LoginService
import io.rsbox.engine.net.login.LoginRequest

/**
 * @author Kyle Escobar
 */

class LoginContext(channel: Channel) : ServerContext(channel) {
    override fun receiveMessage(ctx: ChannelHandlerContext, msg: Any) {
        if(loginService == null) {
            loginService = ServiceManager[LoginService::class.java]
        }

        if(msg is LoginRequest) {
            loginService!!.addLoginRequest(msg)
        }
    }

    override fun terminate() {

    }

    companion object {
        var loginService: LoginService? = null
    }
}