package io.github.antistereov.orbitab.account.user.dto

data class DeviceInfoRequestDto(
    val id: String,
    val browser: String? = null,
    val os: String? = null,
)
