package io.github.antistereov.orbitab.connector.spotify.auth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SpotifyTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("scope") val scope: String,
    @JsonProperty("expires_in") val expiresIn: Long,
    @JsonProperty("refresh_token") val refreshToken: String,
)
