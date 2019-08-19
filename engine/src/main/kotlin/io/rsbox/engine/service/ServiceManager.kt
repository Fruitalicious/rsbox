package io.rsbox.engine.service

import io.rsbox.engine.service.impl.XteaKeyService
import io.rsbox.engine.service.impl.login.LoginService
import mu.KLogging

/**
 * @author Kyle Escobar
 */

object ServiceManager : KLogging() {
    private val services = mutableListOf<Service>()

    fun init() {
        logger.info("Loading services...")

        register(LoginService::class.java)
        register(XteaKeyService::class.java)
    }

    private fun register(service: Class<out Service>) {
        val inst = service.newInstance()
        inst.loaded = true
        services.add(inst)
        inst.start()

        logger.info("Starting service {}.", service.simpleName)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Service> get(service: Class<out T>): T? = services.associate { it::class.java to it }[service] as T
}