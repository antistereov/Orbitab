package io.github.antistereov.orbitab.account.account.model

enum class Role(private val value: String) {
    USER("USER"),
    ADMIN("ADMIN"),
    GUEST("GUEST");

    override fun toString(): String {
        return this.value
    }
}