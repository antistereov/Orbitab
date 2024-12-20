package io.github.antistereov.orbitab.account.account.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "guests")
data class GuestDocument(
    @Id override val id: String? = null,
    override val accountType: AccountType = AccountType.GUEST,
    override val roles: List<Role> = listOf(Role.GUEST),
    override val lastActive: Instant = Instant.now(),
    @Indexed(unique = true) val deviceId: String,
    val refreshToken: String? = null,
) : AccountDocument()
