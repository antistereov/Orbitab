package io.github.antistereov.orbitab.auth.filter

import io.github.antistereov.orbitab.account.account.service.AccountService
import io.github.antistereov.orbitab.auth.exception.InvalidTokenException
import io.github.antistereov.orbitab.auth.model.CustomAuthenticationToken
import io.github.antistereov.orbitab.auth.service.TokenService
import io.github.antistereov.orbitab.config.Constants
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class CookieAuthenticationFilter(
    private val tokenService: TokenService,
    private val accountService: AccountService,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain) = mono {
        if (exchange.attributes.containsKey("cookieAuthenticationProcessed")) {
            chain.filter(exchange).awaitFirstOrNull()
        }
        exchange.attributes["cookieAuthenticationProcessed"] = true

        val authToken = extractTokenFromRequest(exchange)

        if (!authToken.isNullOrBlank()) {
            val accessToken = tokenService.validateAndExtractAccessToken(authToken)

            val account = accountService.findByIdOrNull(accessToken.accountId, accessToken.accountType)
                ?: throw InvalidTokenException("Access token belongs to account that does not exist")

            val authentication = CustomAuthenticationToken(account)

            val securityContext = SecurityContextImpl(authentication)
            return@mono chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                .awaitFirstOrNull()
        }

        chain.filter(exchange).awaitFirstOrNull()
    }

    /**
     * Extract token from request. The header has higher priority than the cookie.
     */
    private fun extractTokenFromRequest(exchange: ServerWebExchange): String? {
        return exchange.request.headers["Authorization"]
            ?.firstOrNull()?.removePrefix("Bearer ")
            ?: exchange.request.cookies[Constants.ACCESS_TOKEN_COOKIE]?.firstOrNull()?.value
    }
}