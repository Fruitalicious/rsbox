package io.rsbox.engine.net.login

import io.rsbox.engine.model.entity.Player
import io.rsbox.util.IsaacRandom

/**
 * @author Kyle Escobar
 */

data class LoginResponse(val player: Player, val encodeRandom: IsaacRandom, val decodeRandom: IsaacRandom)