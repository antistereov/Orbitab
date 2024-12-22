package io.github.antistereov.orbitab.account.account.service

import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.auth.exception.InvalidTokenException
import io.github.antistereov.orbitab.auth.model.RefreshToken
import io.github.antistereov.orbitab.auth.properties.JwtProperties
import io.github.antistereov.orbitab.auth.service.TokenService
import io.github.antistereov.orbitab.config.Constants
import io.github.antistereov.orbitab.config.properties.BackendProperties
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseCookie
import org.springframework.web.server.ServerWebExchange

abstract class AccountSessionService(
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    private val backendProperties: BackendProperties,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    fun createAccessTokenCookie(accountId: String, accountType: AccountType): ResponseCookie {
        logger.debug { "Creating access token cookie for account $accountId" }

        val accessToken = tokenService.createAccessToken(accountId, accountType)

        val cookie = ResponseCookie.from(Constants.ACCESS_TOKEN_COOKIE, accessToken)
            .httpOnly(true)
            .sameSite("Strict")
            .maxAge(jwtProperties.expiresIn)
            .path("/")

        if (backendProperties.secure) {
            cookie.secure(true)
        }
        return cookie.build()
    }

    fun clearAccessTokenCookie(): ResponseCookie {
        logger.debug { "Clearing access token cookie" }

        val cookie = ResponseCookie.from(Constants.ACCESS_TOKEN_COOKIE, "")
            .httpOnly(true)
            .sameSite("Strict")
            .maxAge(0)
            .path("/")

        if (backendProperties.secure) {
            cookie.secure(true)
        }

        return cookie.build()

    }

    fun clearRefreshTokenCookie(): ResponseCookie {
        logger.debug { "Clearing refresh token cookie" }

        val cookie = ResponseCookie.from(Constants.REFRESH_TOKEN_COOKIE, "")
            .httpOnly(true)
            .sameSite("Strict")
            .maxAge(0)
            .path("/auth/refresh")

        if (backendProperties.secure) {
            cookie.secure(true)
        }

        return cookie.build()
    }

    abstract suspend fun validateRefreshToken(refreshToken: RefreshToken): Boolean

    suspend fun validateAndExtractRefreshToken(exchange: ServerWebExchange, deviceId: String): RefreshToken {
        logger.debug { "Validating and extracting refresh token" }

        val refreshTokenCookie = exchange.request.cookies[Constants.REFRESH_TOKEN_COOKIE]?.firstOrNull()?.value
            ?: throw InvalidTokenException("No refresh token provided")

        val refreshToken = tokenService.extractRefreshToken(refreshTokenCookie, deviceId)

        val validated = validateRefreshToken(refreshToken)

        if (!validated) throw InvalidTokenException("Refresh token is invalid")

        return refreshToken
    }
}