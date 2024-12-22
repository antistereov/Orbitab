package io.github.antistereov.orbitab

import io.github.antistereov.orbitab.account.account.model.AccountDocument
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.guest.model.GuestDocument
import io.github.antistereov.orbitab.account.guest.service.GuestService
import io.github.antistereov.orbitab.account.user.dto.DeviceInfoRequestDto
import io.github.antistereov.orbitab.account.user.dto.RegisterUserDto
import io.github.antistereov.orbitab.account.user.model.UserDocument
import io.github.antistereov.orbitab.account.user.service.UserService
import io.github.antistereov.orbitab.config.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BaseIntegrationTest {

    @Autowired
    private lateinit var guestService: GuestService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    lateinit var webTestClient: WebTestClient

    companion object {
        private val mongoDBContainer = MongoDBContainer("mongo:latest").apply {
            start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { "${mongoDBContainer.connectionString}/test" }
            registry.add("jwt.expires-in") { 900 }
            registry.add("jwt.secret-key") { "64f09a172d31b6253d0af2e7dccce6bc9e4e55f8043df07c3ebda72c262758662c2c076e9f11965f43959186b9903fa122da44699b38e40ec21b4bd2fc0ad8c93be946d3dcd0208a1a3ae9d39d4482674d56f6e6dddfe8a6321ad31a824b26e3d528943b0943ad3560d23a79da1fefde0ee2a20709437cedee9def79d5b4c0cf96ee36c02b67ab5fd28638606a5c19ffe8b76d40077549f6db6920a97da0089f5cd2d28665e1d4fb6d9a68fe7b78516a8fc8c33d6a6dac53a77ab670e3449cb237a49104478b717e20e1d22e270f7cf06f9b412b55255c150cb079365eadaddd319385d6221e4b40ed89cdcde540538ce88e66ae2f783c3c48859a14ec6eff83" }
            registry.add("encryption.secret-key") { "3eJAiq7XBjMc5AXkCwsjbA==" }
        }
    }

    open class TestRegisterResponse(
        open val info: AccountDocument,
        open val accessToken: String,
        open val refreshToken: String,
        )
    data class TestRegisterUserResponse(
        override val info: UserDocument,
        override val accessToken: String,
        override val refreshToken: String,
    ) : TestRegisterResponse(info, accessToken, refreshToken)
    data class TestRegisterGuestResponse(
        override val info: GuestDocument,
        override val accessToken: String,
        override val refreshToken: String,
    ) : TestRegisterResponse(info, accessToken, refreshToken)

    suspend fun registerUser(
        email: String = "test@email.com",
        password: String = "password",
        deviceId: String = "device"
    ): TestRegisterUserResponse {
        val device = DeviceInfoRequestDto(id = deviceId)

        val responseCookies = webTestClient.post()
            .uri("/user/register")
            .bodyValue(RegisterUserDto(email, password, device))
            .exchange()
            .expectStatus().isOk
            .returnResult<Void>()
            .responseCookies

        val user = userService.findByEmail(email)
        val accessToken = responseCookies[Constants.ACCESS_TOKEN_COOKIE]?.firstOrNull()?.value
        val refreshToken = responseCookies[Constants.REFRESH_TOKEN_COOKIE]?.firstOrNull()?.value

        requireNotNull(user) { "User associated to $email not saved" }
        requireNotNull(accessToken) { "No access token contained in response" }
        requireNotNull(refreshToken) { "No refresh token contained in response" }

        return TestRegisterUserResponse(user, accessToken, refreshToken)
    }

    suspend fun registerGuest(deviceId: String = "device"): TestRegisterGuestResponse {
        val device = DeviceInfoRequestDto(id = deviceId)

        val responseCookies = webTestClient.post()
            .uri("/guest/login")
            .bodyValue(device)
            .exchange()
            .expectStatus().isOk
            .returnResult<Void>()
            .responseCookies

        val guest = guestService.findByDeviceIdOrNull(device.id)
        val accessToken = responseCookies[Constants.ACCESS_TOKEN_COOKIE]?.firstOrNull()?.value
        val refreshToken = responseCookies[Constants.REFRESH_TOKEN_COOKIE]?.firstOrNull()?.value

        requireNotNull(guest) { "User associated to $device not saved" }
        requireNotNull(accessToken) { "No access token contained in response" }
        requireNotNull(refreshToken) { "No refresh token contained in response" }

        return TestRegisterGuestResponse(guest, accessToken, refreshToken)
    }

    suspend fun deleteAccount(response: TestRegisterResponse) {
        when (response.info.accountType) {
            AccountType.REGISTERED -> {
                webTestClient.delete()
                    .uri("/user/me")
                    .header(HttpHeaders.COOKIE, "${Constants.ACCESS_TOKEN_COOKIE}=${response.accessToken}")
                    .exchange()
                    .expectStatus().isOk
            }
            AccountType.GUEST -> {
                webTestClient.delete()
                    .uri("/guest/me")
                    .header(HttpHeaders.COOKIE, "${Constants.ACCESS_TOKEN_COOKIE}=${response.accessToken}")
                    .exchange()
                    .expectStatus().isOk
            }
        }

    }
}