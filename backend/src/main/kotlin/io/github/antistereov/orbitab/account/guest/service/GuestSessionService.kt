package io.github.antistereov.orbitab.account.guest.service

import io.github.antistereov.orbitab.account.account.exception.AccountException
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.account.service.AccountSessionService
import io.github.antistereov.orbitab.account.guest.model.GuestDocument
import io.github.antistereov.orbitab.auth.model.RefreshToken
import io.github.antistereov.orbitab.auth.properties.JwtProperties
import io.github.antistereov.orbitab.auth.service.TokenService
import io.github.antistereov.orbitab.config.Constants
import io.github.antistereov.orbitab.config.properties.BackendProperties
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class GuestSessionService(
    private val guestService: GuestService,
    private val tokenService: TokenService,
    jwtProperties: JwtProperties,
    private val backendProperties: BackendProperties,
    ) : AccountSessionService(tokenService, jwtProperties, backendProperties) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun loginOrRegisterGuestAndGetGuestId(deviceId: String): String {
        logger.debug { "Registering guest $deviceId" }

        val guest = guestService.findByDeviceIdOrNull(deviceId)
            ?: guestService.save(GuestDocument(deviceId = deviceId))

        val guestId = guest.id ?: throw AccountException("No ID provided in GuestDocument")

        return guestId
    }

    suspend fun createRefreshTokenCookie(guestId: String, deviceId: String): ResponseCookie {
        val refreshToken = tokenService.createRefreshToken(guestId, AccountType.GUEST, deviceId)

        val cookie = ResponseCookie.from(Constants.REFRESH_TOKEN_COOKIE, refreshToken)
            .httpOnly(true)
            .sameSite("Strict")
            .path("/auth/refresh")

        if (backendProperties.secure) {
            cookie.secure(true)
        }

        val guest = guestService.findById(guestId)
        guestService.save(guest.copy(lastActive = Instant.now(), refreshToken = refreshToken))

        return cookie.build()
    }

    suspend fun logout(guestId: String) {
        return guestService.deleteById(guestId)
    }

    override suspend fun validateRefreshToken(refreshToken: RefreshToken): Boolean {
        val guest = guestService.findById(refreshToken.accountId)

        return refreshToken.deviceId == guest.deviceId && guest.refreshToken == refreshToken.value
    }
}