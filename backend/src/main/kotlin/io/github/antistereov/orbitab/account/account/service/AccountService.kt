package io.github.antistereov.orbitab.account.account.service

import io.github.antistereov.orbitab.account.account.model.AccountDocument
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.guest.service.GuestService
import io.github.antistereov.orbitab.account.user.service.UserService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val guestService: GuestService,
    private val userService: UserService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun findById(accountId: String, accountType: AccountType): AccountDocument {
        logger.debug { "Finding account $accountId by ID" }

        return when (accountType) {
            AccountType.REGISTERED -> userService.findById(accountId)
            AccountType.GUEST -> guestService.findById(accountId)
        }
    }

    suspend fun findByIdOrNull(accountId: String, accountType: AccountType): AccountDocument? {
        logger.debug { "Finding account $accountId by ID" }

        return when (accountType) {
            AccountType.REGISTERED -> userService.findByIdOrNull(accountId)
            AccountType.GUEST -> guestService.findByIdOrNull(accountId)
        }
    }
}
