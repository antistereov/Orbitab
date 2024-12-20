package io.github.antistereov.orbitab.account.account.exception

open class AccountException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)