package io.github.antistereov.orbitab.account.account.controller

import io.github.antistereov.orbitab.account.account.dto.AuthInfo
import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/account")
class AccountController(
    private val authenticationService: AuthenticationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/check")
    suspend fun checkAuthentication(): ResponseEntity<AuthInfo> {
        logger.info { "Checking authentication" }

        return try {
            val accountId = authenticationService.getCurrentAccountId()
            val accountType = authenticationService.getCurrentAccountType()
            ResponseEntity.ok(AuthInfo(accountId, accountType, true))
        } catch (ex: Exception) {
            ResponseEntity.ok(
                AuthInfo(null, null, false)
            )
        }
    }
}
