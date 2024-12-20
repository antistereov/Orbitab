package io.github.antistereov.orbitab.account.account.controller

import io.github.antistereov.orbitab.account.account.model.AccountDocument
import io.github.antistereov.orbitab.account.account.service.AccountService
import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
class AccountController(
    private val accountService: AccountService,
    private val authenticationService: AuthenticationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/me")
    suspend fun getAccount(): ResponseEntity<AccountDocument> {
        val userId = authenticationService.getCurrentAccountId()
        val accountType = authenticationService.getCurrentAccountType()

        return ResponseEntity.ok(
            accountService.findById(userId, accountType)
        )
    }

    @DeleteMapping("/me")
    suspend fun deleteUser(): ResponseEntity<Any> {
        val userId = authenticationService.getCurrentAccountId()
        val accountType = authenticationService.getCurrentAccountType()

        return ResponseEntity.ok(
            accountService.delete(userId, accountType)
        )
    }

    @GetMapping("/check")
    suspend fun checkAuthentication(): ResponseEntity<Map<String, String>> {
        logger.info { "Checking authentication" }

        return try {
            val accountId = authenticationService.getCurrentAccountId()
            val accountType = authenticationService.getCurrentAccountType()
            ResponseEntity.ok(mapOf(
                "status" to "authenticated",
                "account_id" to accountId,
                "account_type" to accountType.toString()
            ))
        } catch (ex: Exception) {
            ResponseEntity.status(401).body(mapOf("status" to "unauthenticated"))
        }
    }
}
