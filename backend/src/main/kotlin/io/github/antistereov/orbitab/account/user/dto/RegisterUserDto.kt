package io.github.antistereov.orbitab.account.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank


data class RegisterUserDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank(message = "Password is required")
    val password: String,
    val deviceInfoDto: DeviceInfoRequestDto,
)
