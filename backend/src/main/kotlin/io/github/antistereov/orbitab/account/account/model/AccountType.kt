package io.github.antistereov.orbitab.account.account.model

enum class AccountType(val value: String) {
    REGISTERED("REGISTERED"),
    GUEST("GUEST");

    override fun toString(): String {
        return value
    }
}