package io.github.antistereov.orbitab.account.account.model

import io.github.antistereov.orbitab.account.account.model.tile.Tile
import io.github.antistereov.orbitab.account.account.model.tile.TileConfig
import io.github.antistereov.orbitab.account.user.model.DeviceInfo
import io.github.antistereov.orbitab.connector.shared.model.ConnectorInformation
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "users")
data class UserDocument(
    @Id override val id: String? = null,
    override val accountType: AccountType = AccountType.REGISTERED,
    @Indexed(unique = true) val email: String,
    val password: String,
    override val roles: List<Role> = listOf(Role.USER),
    val connectors: ConnectorInformation? = null,
    val devices: List<DeviceInfo> = listOf(),
    override val lastActive: Instant = Instant.now(),
    override val tiles: List<Tile<TileConfig>> = listOf(),
) : AccountDocument()
