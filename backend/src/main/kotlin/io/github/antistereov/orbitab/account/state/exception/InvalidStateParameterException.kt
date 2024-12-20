package io.github.antistereov.orbitab.account.state.exception

import io.github.antistereov.orbitab.account.account.exception.AccountException

class InvalidStateParameterException(message: String, cause: Throwable? = null) : AccountException(message, cause)