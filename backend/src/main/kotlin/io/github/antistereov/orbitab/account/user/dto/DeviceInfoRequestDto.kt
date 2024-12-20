package io.github.antistereov.orbitab.account.user.dto

data class DeviceInfoRequestDto(
    val deviceId: String,
    val browser: String? = null,
    val os: String? = null,
)
