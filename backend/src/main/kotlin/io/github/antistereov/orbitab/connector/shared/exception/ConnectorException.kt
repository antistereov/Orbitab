package io.github.antistereov.orbitab.connector.shared.exception

import io.github.antistereov.orbitab.global.exception.OrbitabException

open class ConnectorException(message: String? = null, cause: Throwable? = null) : OrbitabException(message, cause)