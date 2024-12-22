package io.github.antistereov.orbitab.account.user.model

import io.github.antistereov.orbitab.account.user.dto.DeviceInfoRequestDto

data class DeviceInfo(
    val id: String,
    val tokenValue: String? = null,
    val browser: String? = null,
    val os: String? = null,
    val issuedAt: Long,
    val ipAddress: String?,
    val location: LocationInfo?,
) {
    data class LocationInfo(
        val latitude: Float,
        val longitude: Float,
        val cityName: String,
        val regionName: String,
        val countryCode: String,
    )

    fun toDto(): DeviceInfoRequestDto {
        return DeviceInfoRequestDto(
            id = id,
            browser = browser,
            os = os,
        )
    }
}
