package io.github.antistereov.orbitab.account.user.dto

data class LoginUserDto(
    val username: String,
    val password: String,
    val deviceInfoDto: DeviceInfoRequestDto
)