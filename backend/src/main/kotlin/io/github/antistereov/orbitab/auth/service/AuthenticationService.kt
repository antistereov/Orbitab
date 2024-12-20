package io.github.antistereov.orbitab.auth.service

import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.auth.exception.InvalidPrincipalException
import io.github.antistereov.orbitab.auth.exception.InvalidTokenException
import io.github.antistereov.orbitab.auth.model.CustomAuthenticationToken
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Service

@Service
class AuthenticationService {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getCurrentAccountId(): String {
        logger.debug {"Extracting user ID." }

        val auth = getCurrentAuthentication()
        return auth.name ?: throw InvalidPrincipalException("Missing or invalid authentication principal.")
    }

    suspend fun getCurrentAccountType(): AccountType {
        logger.debug { "Extracting account type" }

        val auth = getCurrentAuthentication()
        return auth.accountType
    }

    private suspend fun getCurrentAuthentication(): CustomAuthenticationToken {
        val securityContext: SecurityContext = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()
            ?: throw InvalidPrincipalException("No security context found.")

        val authentication = securityContext.authentication
            ?: throw InvalidTokenException("Authentication is missing.")

        return authentication as? CustomAuthenticationToken
                ?: throw InvalidTokenException("Authentication does not contain needed properties")

    }
}