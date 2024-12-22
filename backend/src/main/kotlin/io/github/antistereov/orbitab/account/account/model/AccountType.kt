package io.github.antistereov.orbitab.account.account.model

import io.github.antistereov.orbitab.account.account.exception.AccountException

enum class AccountType(val value: String) {
    REGISTERED("REGISTERED"),
    GUEST("GUEST");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): AccountType {
            return when(value) {
                "REGISTERED" -> REGISTERED
                "GUEST" -> GUEST
                else -> throw AccountException("Illegal account type: $value")
            }
        }
    }
}