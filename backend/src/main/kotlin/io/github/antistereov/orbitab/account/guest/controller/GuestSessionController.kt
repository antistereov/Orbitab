package io.github.antistereov.orbitab.account.guest.controller

import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.guest.service.GuestSessionService
import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/guest")
class GuestSessionController(
    private val guestSessionService: GuestSessionService,
    private val authenticationService: AuthenticationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @PostMapping("/login")
    suspend fun loginOrRegister(@RequestParam deviceId: String): ResponseEntity<Map<String, String>> {
        logger.info { "Executing login or register" }

        val guestId = guestSessionService.loginOrRegisterGuestAndGetGuestId(deviceId)

        val accessTokenCookie = guestSessionService.createAccessTokenCookie(guestId, AccountType.GUEST)
        val refreshTokenCookie = guestSessionService.createRefreshTokenCookie(guestId, deviceId)

        return ResponseEntity.ok()
            .header("Set-Cookie", accessTokenCookie.toString())
            .header("Set-Cookie", refreshTokenCookie.toString())
            .body(mapOf(
                "account_id" to guestId,
                "account_type" to AccountType.GUEST.toString(),
                "access_token" to accessTokenCookie.value,
                "refresh_token" to refreshTokenCookie.value
            ))
    }

    @PostMapping("/logout")
    suspend fun logout(): ResponseEntity<Map<String, String>> {
        logger.debug { "Executing logout method" }

        val clearAccessTokenCookie = guestSessionService.clearAccessTokenCookie()
        val clearRefreshTokenCookie = guestSessionService.clearRefreshTokenCookie()

        val guestId = authenticationService.getCurrentAccountId()
        guestSessionService.logout(guestId)

        return ResponseEntity.ok()
            .header("Set-Cookie", clearAccessTokenCookie.toString())
            .header("Set-Cookie", clearRefreshTokenCookie.toString())
            .body(mapOf("message" to "Logout successful"))
    }

    @PostMapping("/refresh")
    suspend fun refresh(
        exchange: ServerWebExchange,
        @RequestParam deviceId: String
    ): ResponseEntity<Map<String, String>> {
        logger.debug { "Refreshing access token" }

        val refreshToken = guestSessionService.validateAndExtractRefreshToken(exchange, deviceId)

        val newAccessToken = guestSessionService.createAccessTokenCookie(refreshToken.accountId, AccountType.GUEST)
        val newRefreshToken = guestSessionService.createRefreshTokenCookie(refreshToken.accountId, deviceId)

        return ResponseEntity.ok()
            .header("Set-Cookie", newAccessToken.toString())
            .header("Set-Cookie", newRefreshToken.toString())
            .body(mapOf("access_token" to newAccessToken.value, "refresh_token" to newRefreshToken.value))
    }
}