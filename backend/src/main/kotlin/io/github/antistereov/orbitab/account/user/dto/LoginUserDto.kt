package io.github.antistereov.orbitab.account.user.dto

data class LoginUserDto(
    val email: String,
    val password: String,
    val device: DeviceInfoRequestDto
)