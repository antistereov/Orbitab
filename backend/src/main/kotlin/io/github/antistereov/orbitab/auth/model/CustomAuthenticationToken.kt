package io.github.antistereov.orbitab.auth.model

import io.github.antistereov.orbitab.account.account.model.AccountDocument
import io.github.antistereov.orbitab.account.account.model.AccountType
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class CustomAuthenticationToken(
    val userId: String,
    val accountType: AccountType,
    authorities: Collection<GrantedAuthority>
) : AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null
    override fun getPrincipal(): String = userId

    constructor(accountDocument: AccountDocument): this(
        userId = accountDocument.id!!, // TODO: catch error
        accountType = accountDocument.accountType,
        authorities = accountDocument.roles.map { SimpleGrantedAuthority("ROLE_$it") }
    )
}