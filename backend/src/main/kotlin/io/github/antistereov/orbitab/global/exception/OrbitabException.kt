package io.github.antistereov.orbitab.global.exception

open class OrbitabException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)