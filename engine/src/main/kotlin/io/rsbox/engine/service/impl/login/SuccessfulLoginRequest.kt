package io.rsbox.engine.service.impl.login

import io.rsbox.engine.model.entity.Player
import io.rsbox.util.IsaacRandom

data class SuccessfulLoginRequest(val player: Player, val encodeRandom: IsaacRandom, val decodeRandom: IsaacRandom)