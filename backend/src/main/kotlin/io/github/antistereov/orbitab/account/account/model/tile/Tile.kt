package io.github.antistereov.orbitab.account.account.model.tile

data class Tile<T: TileConfig>(
    val type: TileType,
    val config: T
)
