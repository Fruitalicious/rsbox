package io.rsbox.engine

/**
 * @author Kyle Escobar
 */

object EngineConstants {
    /**
     * File paths
     */
    const val CACHE_PATH = "rsbox/data/cache/"
    const val XTEAS_FILE = "rsbox/data/xteas/xteas.json"
    const val RSA_PRIVATE_FILE = "rsbox/data/rsa/private.pem"
    const val RSA_PUBLIC_FILE = "rsbox/data/rsa/public.pem"
    const val RSA_MODULUS_FILE = "rsbox/data/rsa/modulus.txt"
    const val PLAYER_SAVES_PATH = "rsbox/data/saves/"

    /**
     * Repos
     */
    const val CACHE_REPO = "https://github.com/rsbox/cache/raw/rev_<>/cache.zip"
    const val XTEAS_REPO = "https://github.com/rsbox/cache/raw/rev_<>/xteas.json"
}