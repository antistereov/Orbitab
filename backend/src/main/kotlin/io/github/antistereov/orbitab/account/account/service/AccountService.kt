package io.github.antistereov.orbitab.account.account.service

import io.github.antistereov.orbitab.account.account.model.AccountDocument
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.account.model.GuestDocument
import io.github.antistereov.orbitab.account.guest.service.GuestService
import io.github.antistereov.orbitab.account.account.model.UserDocument
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
            AccountType.GUEST -> userService.findByIdOrNull(accountId)
        }
    }

    suspend fun delete(accountId: String, accountType: AccountType) {
        logger.debug { "Deleting account $accountId" }

        when (accountType) {
            AccountType.REGISTERED -> userService.delete(accountId)
            AccountType.GUEST -> guestService.deleteById(accountId)
        }
    }

    suspend fun validateRefreshToken(
        accountId: String,
        accountType: AccountType,
        refreshToken: String,
        deviceId: String
    ): Boolean {
        return when (val account = findById(accountId, accountType)) {
            is GuestDocument -> deviceId == account.deviceId && account.refreshToken == refreshToken
            is UserDocument -> account.devices.any { it.deviceId == deviceId && it.tokenValue == refreshToken }
        }
    }
}
