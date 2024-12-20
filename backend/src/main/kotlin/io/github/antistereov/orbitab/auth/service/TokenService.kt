package io.github.antistereov.orbitab.auth.service

import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.account.service.AccountService
import io.github.antistereov.orbitab.auth.exception.AccessTokenExpiredException
import io.github.antistereov.orbitab.auth.exception.InvalidTokenException
import io.github.antistereov.orbitab.auth.properties.JwtProperties
import io.github.antistereov.orbitab.auth.model.AccessToken
import io.github.antistereov.orbitab.auth.model.RefreshToken
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class TokenService(
    private val jwtDecoder: ReactiveJwtDecoder,
    private val jwtEncoder: JwtEncoder,
    jwtProperties: JwtProperties,
    private val accountService: AccountService
) {

    private val tokenExpiresInSeconds = jwtProperties.expiresIn

    fun createAccessToken(userId: String, accountType: AccountType): String {
        val jwsHeader = JwsHeader.with { "HS256" }.build()
        val claims = JwtClaimsSet.builder()
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(tokenExpiresInSeconds))
            .subject(userId)
            .claim("account_type", accountType)
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }

    suspend fun validateAndExtractAccessToken(token: String): AccessToken {
        val jwt = jwtDecoder.decode(token).awaitFirstOrNull()
            ?: throw InvalidTokenException("Cannot decode access token")
        val expiresAt = jwt.expiresAt
            ?: throw InvalidTokenException("JWT does not contain expiration information")

        if (expiresAt <= Instant.now()) throw AccessTokenExpiredException("Access token is expired")

        val accountId = jwt.subject
            ?: throw InvalidTokenException("JWT does not contain sub")
        val accountType = jwt.claims["account_type"] as? AccountType
            ?: throw InvalidTokenException("JWT does not contain valid account type")

        return AccessToken(accountId, accountType)
    }

    fun createRefreshToken(accountId: String, accountType: AccountType, deviceId: String): String {
        val jwsHeader = JwsHeader.with { "HS256" }.build()

        val claims = JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .subject(accountId)
            .claim("account_type", accountType)
            .claim("device_id", deviceId)
            .issuedAt(Instant.now())
            .build()

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }

    suspend fun validateAndExtractRefreshToken(refreshToken: String, deviceId: String): RefreshToken {
        val jwt = jwtDecoder.decode(refreshToken).awaitFirstOrNull()
            ?: throw InvalidTokenException("Cannot decode refresh token")

        val accountId = jwt.subject
            ?: throw InvalidTokenException("Refresh token does not contain user id")
        val accountType = jwt.claims["account_type"] as? AccountType
            ?: throw InvalidTokenException("JWT does not contain valid account type")

        accountService.validateRefreshToken(accountId, accountType, refreshToken, deviceId)

        return RefreshToken(accountId, accountType, deviceId, refreshToken)
    }
}