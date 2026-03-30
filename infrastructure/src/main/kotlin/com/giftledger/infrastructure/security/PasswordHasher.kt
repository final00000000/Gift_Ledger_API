package com.giftledger.infrastructure.security

import com.giftledger.infrastructure.config.Env
import de.mkammerer.argon2.Argon2Factory

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, hash: String): Boolean
}

class PasswordHasherImpl(
    private val iterations: Int = 3,
    private val memoryKiB: Int = 65_536,
    private val parallelism: Int = 1,
    private val saltLength: Int = 16,
    private val hashLength: Int = 32,
) : PasswordHasher {
    override fun hash(password: String): String {
        val passwordChars = password.toCharArray()
        val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, saltLength, hashLength)
        try {
            return argon2.hash(iterations, memoryKiB, parallelism, passwordChars)
        } finally {
            argon2.wipeArray(passwordChars)
        }
    }

    override fun verify(password: String, hash: String): Boolean {
        val passwordChars = password.toCharArray()
        val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
        try {
            return argon2.verify(hash, passwordChars)
        } catch (_: RuntimeException) {
            return false
        } finally {
            argon2.wipeArray(passwordChars)
        }
    }
}

