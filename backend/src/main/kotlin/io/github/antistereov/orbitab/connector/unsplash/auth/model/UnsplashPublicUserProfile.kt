package io.github.antistereov.orbitab.connector.unsplash.auth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class UnsplashPublicUserProfile(
    val username: String,
    @JsonProperty("profile_image")
    val profileImage: UnsplashProfileImage?,
)