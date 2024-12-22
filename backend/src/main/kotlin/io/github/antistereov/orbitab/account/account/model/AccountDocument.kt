package io.github.antistereov.orbitab.account.account.model

import io.github.antistereov.orbitab.account.account.model.tile.Tile
import io.github.antistereov.orbitab.account.account.model.tile.TileConfig
import java.time.Instant

sealed class AccountDocument {
    abstract val id: String?
    abstract val accountType: AccountType
    abstract val roles: List<Role>
    abstract val lastActive: Instant
    abstract val tiles: List<Tile<TileConfig>>
}