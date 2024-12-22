package io.github.antistereov.orbitab.account.guest.model

import io.github.antistereov.orbitab.account.account.model.AccountDocument
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.account.model.Role
import io.github.antistereov.orbitab.account.account.model.tile.Tile
import io.github.antistereov.orbitab.account.account.model.tile.TileConfig
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
    override val tiles: List<Tile<TileConfig>> = listOf(),
) : AccountDocument()
