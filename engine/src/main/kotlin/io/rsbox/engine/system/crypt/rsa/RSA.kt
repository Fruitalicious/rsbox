package io.rsbox.engine.system.crypt.rsa

import com.uchuhimo.konf.Config
import io.rsbox.engine.EngineConstants
import io.rsbox.engine.config.spec.ServerSpec
import mu.KLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec

/**
 * @author Kyle Escobar
 *
 * Credits to Tomm0017 for his implementation
 */

object RSA : KLogging() {
    lateinit var exponent: BigInteger

    lateinit var modulus: BigInteger

    lateinit var publicKey: RSAPublicKey

    lateinit var privateKey: RSAPrivateKey

    private const val radix = 16
    private val bits = Config{addSpec(ServerSpec)}[ServerSpec.rsa_bits]
    private val privateKeyPath = Paths.get("../${EngineConstants.RSA_PRIVATE_FILE}")
    private val publicKeyPath = Paths.get("../${EngineConstants.RSA_PUBLIC_FILE}")
    private val modulusPath = Paths.get("../${EngineConstants.RSA_MODULUS_FILE}")

    fun load() {
        val file = File(EngineConstants.RSA_PRIVATE_FILE)
        if(!file.exists()) {
            logger.error("The RSA keys have not been generated. Please run the [server:setup] gradle task first.")
            System.exit(0)
        }

        try {
            PemReader(Files.newBufferedReader(file.toPath())).use { reader ->
                val pem = reader.readPemObject()
                val keySpec = PKCS8EncodedKeySpec(pem.content)

                Security.addProvider(BouncyCastleProvider())

                val factory = KeyFactory.getInstance("RSA", "BC")

                privateKey = factory.generatePrivate(keySpec) as RSAPrivateKey
                exponent = privateKey.privateExponent
                modulus = privateKey.modulus

                logger.info("Loaded RSA key pairs.")
            }
        } catch(e : Exception) {
            logger.error("Unable to read RSA private key file.", e)
        }
    }

    fun generate() {
        Security.addProvider(BouncyCastleProvider())

        val kpg = KeyPairGenerator.getInstance("RSA", "BC")
        kpg.initialize(bits)
        val kp = kpg.generateKeyPair()

        privateKey = kp.private as RSAPrivateKey
        publicKey = kp.public as RSAPublicKey

        exponent = privateKey.privateExponent
        modulus = privateKey.modulus

        try {
            // Write private key
            PemWriter(Files.newBufferedWriter(privateKeyPath)).use { writer ->
                writer.writeObject(PemObject("RSA PRIVATE KEY", privateKey.encoded))
            }

            // Write public key
            PemWriter(Files.newBufferedWriter(publicKeyPath)).use { writer ->
                writer.writeObject(PemObject("RSA PUBLIC KEY", publicKey.encoded))
            }

            // Write modulus file
            Files.newBufferedWriter(modulusPath).use { writer ->
                writer.write(modulus.toString(radix))
            }
        } catch(e : Exception) {
            logger.error("Error generating RSA key pair files.", e)
        }
    }
}