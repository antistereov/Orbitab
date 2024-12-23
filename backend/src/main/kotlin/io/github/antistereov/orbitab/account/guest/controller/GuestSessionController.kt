package io.github.antistereov.orbitab.account.guest.controller

import io.github.antistereov.orbitab.account.account.dto.AuthInfo
import io.github.antistereov.orbitab.account.account.model.AccountDocument
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.guest.service.GuestService
import io.github.antistereov.orbitab.account.guest.service.GuestSessionService
import io.github.antistereov.orbitab.account.user.model.DeviceInfo
import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/guest")
class GuestSessionController(
    private val guestSessionService: GuestSessionService,
    private val authenticationService: AuthenticationService,
    private val guestService: GuestService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/me")
    suspend fun getAccount(): ResponseEntity<AccountDocument> {
        val accountId = authenticationService.getCurrentAccountId()

        return ResponseEntity.ok(guestService.findById(accountId))
    }

    @PostMapping("/login")
    suspend fun loginOrRegister(@RequestBody deviceInfo: DeviceInfo): ResponseEntity<AuthInfo> {
        logger.info { "Executing login or register" }

        val guestId = guestSessionService.loginOrRegisterGuestAndGetGuestId(deviceInfo.id)

        val accessTokenCookie = guestSessionService.createAccessTokenCookie(guestId, AccountType.GUEST)
        val refreshTokenCookie = guestSessionService.createRefreshTokenCookie(guestId, deviceInfo.id)

        return ResponseEntity.ok()
            .header("Set-Cookie", accessTokenCookie.toString())
            .header("Set-Cookie", refreshTokenCookie.toString())
            .body(AuthInfo(guestId, AccountType.GUEST, true))
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
        @RequestBody deviceInfo: DeviceInfo
    ): ResponseEntity<Map<String, String>> {
        logger.debug { "Refreshing access token" }

        val refreshToken = guestSessionService.validateAndExtractRefreshToken(exchange, deviceInfo.id)

        val newAccessToken = guestSessionService.createAccessTokenCookie(refreshToken.accountId, AccountType.GUEST)
        val newRefreshToken = guestSessionService.createRefreshTokenCookie(refreshToken.accountId, deviceInfo.id)

        return ResponseEntity.ok()
            .header("Set-Cookie", newAccessToken.toString())
            .header("Set-Cookie", newRefreshToken.toString())
            .body(mapOf("access_token" to newAccessToken.value, "refresh_token" to newRefreshToken.value))
    }

    @DeleteMapping("/me")
    suspend fun deleteAccount(): ResponseEntity<Map<String, String>> {
        val guestId = authenticationService.getCurrentAccountId()

        val clearAccessTokenCookie = guestSessionService.clearAccessTokenCookie()
        val clearRefreshTokenCookie = guestSessionService.clearRefreshTokenCookie()

        guestService.deleteById(guestId)

        return ResponseEntity.ok()
            .header("Set-Cookie", clearAccessTokenCookie.toString())
            .header("Set-Cookie", clearRefreshTokenCookie.toString())
            .body(mapOf("message" to "Guest account deleted successfully"))
    }
}