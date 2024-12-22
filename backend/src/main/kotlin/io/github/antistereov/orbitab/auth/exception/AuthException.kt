package io.github.antistereov.orbitab.auth.exception

import io.github.antistereov.orbitab.global.exception.OrbitabException

open class AuthException(
    message: String,
    cause: Throwable? = null
) : OrbitabException(
    message,
    cause
)