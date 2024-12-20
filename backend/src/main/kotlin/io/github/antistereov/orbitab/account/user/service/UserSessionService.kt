package io.github.antistereov.orbitab.account.user.service

import io.github.antistereov.orbitab.account.account.exception.EmailAlreadyExistsException
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.account.model.UserDocument
import io.github.antistereov.orbitab.account.account.service.AccountSessionService
import io.github.antistereov.orbitab.account.user.dto.DeviceInfoRequestDto
import io.github.antistereov.orbitab.account.user.dto.LoginUserDto
import io.github.antistereov.orbitab.account.user.dto.RegisterUserDto
import io.github.antistereov.orbitab.account.user.model.DeviceInfo
import io.github.antistereov.orbitab.auth.exception.AuthException
import io.github.antistereov.orbitab.auth.exception.InvalidCredentialsException
import io.github.antistereov.orbitab.auth.properties.JwtProperties
import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.antistereov.orbitab.auth.service.HashService
import io.github.antistereov.orbitab.auth.service.TokenService
import io.github.antistereov.orbitab.config.properties.BackendProperties
import io.github.antistereov.orbitab.service.geolocation.GeoLocationService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserSessionService(
    private val userService: UserService,
    private val hashService: HashService,
    private val tokenService: TokenService,
    private val geoLocationService: GeoLocationService,
    private val backendProperties: BackendProperties,
    private val authenticationService: AuthenticationService,
    jwtProperties: JwtProperties,
) : AccountSessionService(tokenService, jwtProperties, backendProperties) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun checkCredentialsAndGetUserId(payload: LoginUserDto): String {
        logger.debug { "Logging in user ${payload.username}" }
        val user = userService.findByEmail(payload.username)
            ?: throw InvalidCredentialsException()

        if (!hashService.checkBcrypt(payload.password, user.password)) {
            throw InvalidCredentialsException()
        }

        if (user.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        return user.id
    }

    suspend fun registerUserAndGetUserId(payload: RegisterUserDto): String {
        logger.debug { "Registering user ${payload.email}" }

        if (userService.existsByEmail(payload.email)) {
            throw EmailAlreadyExistsException("Failed to register user ${payload.email}")
        }

        val userDocument = UserDocument(
            email = payload.email,
            password = hashService.hashBcrypt(payload.password)
        )

        val savedUserDocument = userService.save(userDocument)

        if (savedUserDocument.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        return savedUserDocument.id
    }

    suspend fun createRefreshTokenCookie(
        userId: String,
        deviceInfoDto: DeviceInfoRequestDto,
        ipAddress: String?
    ): ResponseCookie {
        val refreshToken = tokenService.createRefreshToken(userId, AccountType.REGISTERED, deviceInfoDto.deviceId)

        val location = ipAddress?.let { geoLocationService.getLocation(it) }

        val deviceInfo = DeviceInfo(
            deviceId = deviceInfoDto.deviceId,
            tokenValue = refreshToken,
            browser = deviceInfoDto.browser,
            os = deviceInfoDto.os,
            issuedAt = System.currentTimeMillis(),
            ipAddress = ipAddress,
            location = if (location != null) {
                DeviceInfo.LocationInfo(
                    location.latitude,
                    location.longitude,
                    location.cityName,
                    location.regionName,
                    location.countryCode
                )
            } else null,
        )

        userService.addOrUpdateDevice(userId, deviceInfo)

        val cookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .sameSite("Strict")
            .path("/auth/refresh")

        if (backendProperties.secure) {
            cookie.secure(true)
        }

        return cookie.build()
    }

    suspend fun logout(deviceId: String): UserDocument {
        val userId = authenticationService.getCurrentAccountId()
        val user = userService.findById(userId)
        val updatedDevices = user.devices.filterNot { it.deviceId == deviceId }

        return userService.save(user.copy(devices = updatedDevices, lastActive = Instant.now()))
    }
}