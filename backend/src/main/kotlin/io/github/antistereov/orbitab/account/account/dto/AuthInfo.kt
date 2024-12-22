package io.github.antistereov.orbitab.account.account.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.antistereov.orbitab.account.account.model.AccountType

data class AuthInfo(
    @JsonProperty("account_id")
    val accountId: String?,
    @JsonProperty("account_type")
    val accountType: AccountType?,
    val authenticated: Boolean,
)
