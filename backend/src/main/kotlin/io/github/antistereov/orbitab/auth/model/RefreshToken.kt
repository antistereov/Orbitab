package io.github.antistereov.orbitab.auth.model

import io.github.antistereov.orbitab.account.account.model.AccountType

data class RefreshToken(
    val accountId: String,
    val accountType: AccountType,
    val deviceId: String,
    val value: String,
)
