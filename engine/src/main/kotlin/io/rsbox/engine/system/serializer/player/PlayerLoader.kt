package io.rsbox.engine.system.serializer.player

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml.toYaml
import io.rsbox.engine.EngineConstants
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.net.login.LoginRequest
import io.rsbox.engine.system.serializer.player.spec.PlayerSpec
import net.openhft.hashing.LongHashFunction

/**
 * @author Kyle Escobar
 */

object PlayerLoader {
    fun createPlayerSave(username: String, password: String) {
        val save = Config { addSpec(PlayerSpec) }
        save[PlayerSpec.username] = filterJagexString(username)
        save[PlayerSpec.password] = LongHashFunction.xx().hashChars(password).toString()
        save[PlayerSpec.displayName] = ""
        save[PlayerSpec.privilege] = 0
        save.toYaml.toFile("${EngineConstants.PLAYER_SAVES_PATH}${filterJagexString(username)}.yml")
    }

    fun loadPlayerFromRequest(request: LoginRequest) : Player {
        val save = Config{addSpec(PlayerSpec)}.from.yaml.file("${EngineConstants.PLAYER_SAVES_PATH}${filterJagexString(request.username)}.yml")

        val player = Player(request.channel)
        player.username = save[PlayerSpec.username]
        player.passwordHash = save[PlayerSpec.password]
        player.displayName = save[PlayerSpec.displayName]
        player.privilege = save[PlayerSpec.privilege]

        return player
    }

    fun checkPassword(username: String, password: String): Boolean {
        val save = Config { addSpec(PlayerSpec) }.from.yaml.file("${EngineConstants.PLAYER_SAVES_PATH}${filterJagexString(username)}.yml")
        val hashedPassword = save[PlayerSpec.password].toLong()

        if(LongHashFunction.xx().hashChars(password) == hashedPassword) {
            return true
        }
        return false
    }

    private fun filterJagexString(string: String) : String {
        return string.replace(" ","_")
    }
}