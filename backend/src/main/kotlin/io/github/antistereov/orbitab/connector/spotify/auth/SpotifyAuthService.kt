package io.github.antistereov.orbitab.connector.spotify.auth

import io.github.antistereov.orbitab.security.AESEncryption
import io.github.antistereov.orbitab.account.state.service.StateService
import io.github.antistereov.orbitab.account.user.service.UserService
import io.github.antistereov.orbitab.connector.shared.model.ConnectorInformation
import io.github.antistereov.orbitab.connector.spotify.auth.model.SpotifyRefreshTokenResponse
import io.github.antistereov.orbitab.connector.spotify.auth.model.SpotifyTokenResponse
import io.github.antistereov.orbitab.connector.spotify.exception.SpotifyTokenException
import io.github.antistereov.orbitab.connector.spotify.exception.SpotifyException
import io.github.antistereov.orbitab.connector.spotify.exception.SpotifyInvalidCallbackException
import io.github.antistereov.orbitab.connector.spotify.model.SpotifyUserProfile
import io.github.antistereov.orbitab.connector.spotify.model.SpotifyUserInformation
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime
import java.util.*

@Service
class SpotifyAuthService(
    private val webClient: WebClient,
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
    private val stateService: StateService,
    private val properties: SpotifyProperties,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getAuthorizationUrl(userId: String): String {
        logger.debug { "Getting authorization URL for user: $userId." }

        val state = stateService.createState(userId)

        return UriComponentsBuilder
            .fromHttpUrl("https://accounts.spotify.com/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", properties.clientId)
            .queryParam("scope", properties.scopes)
            .queryParam("redirect_uri", properties.redirectUri)
            .queryParam("state", state)
            .toUriString()
    }

    suspend fun authenticate(code: String?, state: String?, error: String?): SpotifyUserProfile {

        suspend fun getSpotifyTokenResponse(code: String): SpotifyTokenResponse {
            logger.debug { "Handling authentication." }

            val auth = "${properties.clientId}:${properties.clientSecret}"
            val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
            val uri = "https://accounts.spotify.com/api/token"

            return webClient
                .post()
                .uri(uri)
                .header(
                    "Content-Type",
                    "application/x-www-form-urlencoded")
                .header(
                    "Authorization",
                    "Basic $encodedAuth")
                .body(
                    BodyInserters
                        .fromFormData("grant_type", "authorization_code")
                        .with("code", code)
                        .with("redirect_uri", properties.redirectUri)
                )
                .retrieve()
                .awaitBody<SpotifyTokenResponse>()
        }

        suspend fun handleUser(userId: String, response: SpotifyTokenResponse) {
            logger.debug { "Handling user." }

            val user = userService.findById(userId)

            val refreshToken = response.refreshToken
            val expirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

            val widgetInformation = user.connectors ?: ConnectorInformation()

            val spotifyInfo = widgetInformation.spotify ?: SpotifyUserInformation()

            val updatedSpotifyInfo = spotifyInfo.copy(
                accessToken = aesEncryption.encrypt(response.accessToken),
                refreshToken = aesEncryption.encrypt(refreshToken),
                expirationDate = expirationDate
            )

            val updatedWidgetInformation = widgetInformation.copy(spotify = updatedSpotifyInfo)

            val updatedUser = user.copy(connectors = updatedWidgetInformation)

            userService.save(updatedUser)
        }

        logger.debug { "Authenticating user." }

        if (code == null || state == null) {
            throw SpotifyInvalidCallbackException("Invalid callback from Unsplash: " +
                    "no code or no state passed to callback")
        }

        val spotifyTokenResponse = getSpotifyTokenResponse(code)
        val userId = stateService.getUserId(state)
        handleUser(userId, spotifyTokenResponse)

        return getUserProfile(userId)
    }

    suspend fun disconnect(userId: String) {
        logger.debug { "Logging out user: $userId." }

        val user = userService.findById(userId)

        val widgetsInfo = user.connectors ?: ConnectorInformation()
        val updatedWidgetInfo = widgetsInfo.copy(spotify = null)

        val updatedUser = user.copy(connectors = updatedWidgetInfo)

        userService.save(updatedUser)
    }

    suspend fun getUserProfile(userId: String): SpotifyUserProfile {
        logger.debug { "Fetching Spotify user profile for user $userId" }
        val accessToken = getAccessToken(userId)

        return webClient.get()
            .uri("${properties.apiBaseUrl}/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody<SpotifyUserProfile>()
    }

    suspend fun getAccessToken(userId: String): String {
        logger.debug { "Getting access token for user: $userId." }

        val currentTime = LocalDateTime.now()

        val user = userService.findById(userId)

        val expirationDate = user.connectors?.spotify?.expirationDate
            ?: throw SpotifyException("No expiration date for Spotify access token saved for user $userId")

        return if (currentTime.isAfter(expirationDate)) {
            this.refreshToken(userId).accessToken
        } else {
            val encryptedSpotifyAccessToken = user.connectors.spotify.accessToken
                ?: throw SpotifyTokenException(userId)

            aesEncryption.decrypt(encryptedSpotifyAccessToken)
        }
    }

    private suspend fun refreshToken(userId: String): SpotifyRefreshTokenResponse {
        logger.debug { "Refreshing token for user: $userId." }

        val uri = "https://accounts.spotify.com/api/token"

        val user = userService.findById(userId)

        val encryptedRefreshToken = user.connectors?.spotify?.refreshToken
            ?: throw SpotifyTokenException(userId)

        val refreshToken = aesEncryption.decrypt(encryptedRefreshToken)

        val tokenResponse = webClient.post()
            .uri(uri)
            .header(
                "Content-Type",
                "application/x-www-form-urlencoded")
            .body(
                BodyInserters
                    .fromFormData("grant_type", "refresh_token")
                    .with("refresh_token", refreshToken)
                    .with("client_id", properties.clientId)
                    .with("client_secret", properties.clientSecret)
            )
            .retrieve()
            .awaitBody<SpotifyRefreshTokenResponse>()

        val encryptedUpdatedAccessToken = aesEncryption.encrypt(tokenResponse.accessToken)
        val expirationDate = LocalDateTime.now().plusSeconds(tokenResponse.expiresIn)

        val updatedUser = user.copy(
            connectors = user.connectors.copy(
                spotify = user.connectors.spotify.copy(
                    accessToken = encryptedUpdatedAccessToken,
                    expirationDate = expirationDate,
                )
            )
        )

        userService.save(updatedUser)

        return tokenResponse
    }
}