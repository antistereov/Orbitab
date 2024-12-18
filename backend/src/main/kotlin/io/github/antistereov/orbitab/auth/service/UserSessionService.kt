package io.github.antistereov.orbitab.auth.service

import io.github.antistereov.orbitab.auth.model.UserSessionCookieData
import io.github.antistereov.orbitab.auth.exception.AuthException
import io.github.antistereov.orbitab.auth.exception.InvalidCredentialsException
import io.github.antistereov.orbitab.auth.properties.JwtProperties
import io.github.antistereov.orbitab.user.dto.LoginUserDto
import io.github.antistereov.orbitab.user.dto.RegisterUserDto
import io.github.antistereov.orbitab.user.exception.UsernameAlreadyExistsException
import io.github.antistereov.orbitab.user.model.UserDocument
import io.github.antistereov.orbitab.user.service.UserService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserSessionService(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val hashService: HashService,
    private val jwtProperties: JwtProperties,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun login(payload: LoginUserDto): UserSessionCookieData {
        logger.debug { "Logging in user ${payload.username}" }
        val user = userService.findByUsername(payload.username)
            ?: throw InvalidCredentialsException()

        if (!hashService.checkBcrypt(payload.password, user.password)) {
            throw InvalidCredentialsException()
        }

        if (user.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        logger.debug { "Successfully logged in user ${payload.username}" }

        return UserSessionCookieData(
            accessToken = tokenService.createToken(user.id),
            expiresIn = jwtProperties.expiresIn,
        )
    }

    suspend fun register(payload: RegisterUserDto): UserSessionCookieData {
        logger.debug { "Registering user ${payload.username}" }

        if (userService.existsByUsername(payload.username)) {
            throw UsernameAlreadyExistsException("Failed to register user ${payload.username}")
        }

        val userDocument = UserDocument(
            username = payload.username,
            password = hashService.hashBcrypt(payload.password)
        )

        val savedUserDocument = userService.save(userDocument)

        if (savedUserDocument.id == null) {
            throw AuthException("Login failed: UserDocument contains no id")
        }

        logger.debug { "Successfully registered user ${payload.username}" }

        return UserSessionCookieData(
            accessToken = tokenService.createToken(savedUserDocument.id),
            expiresIn = jwtProperties.expiresIn,
        )
    }
}