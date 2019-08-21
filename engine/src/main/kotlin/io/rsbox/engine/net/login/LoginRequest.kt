package io.rsbox.engine.net.login

import io.netty.channel.Channel

/**
 * @author Kyle Escobar
 */

class LoginRequest(
    val channel: Channel,
    val username: String,
    val password: String,
    val revision: Int,
    val xteaKeys: IntArray,
    val authCode: Int,
    val uuid: String,
    val reconnecting: Boolean,
    val resizableClient: Boolean,
    val clientWidth: Int,
    val clientHeight: Int
)