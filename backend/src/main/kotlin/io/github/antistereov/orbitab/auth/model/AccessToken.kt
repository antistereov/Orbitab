package io.github.antistereov.orbitab.auth.model

import io.github.antistereov.orbitab.account.account.model.AccountType

data class AccessToken(
    val userId: String,
    val accountType: AccountType,
)
