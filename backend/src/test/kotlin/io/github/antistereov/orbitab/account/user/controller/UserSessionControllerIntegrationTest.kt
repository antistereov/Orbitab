package io.github.antistereov.orbitab.account.user.controller

import io.github.antistereov.orbitab.BaseIntegrationTest
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.user.model.UserDocument
import io.github.antistereov.orbitab.config.Constants
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class UserSessionControllerIntegrationTest : BaseIntegrationTest() {

    @Test fun `getAccount returns user account`() = runTest {
        val user = registerUser()

        val responseBody = webTestClient.get()
            .uri("/user/me")
            .header(HttpHeaders.COOKIE, "${Constants.ACCESS_TOKEN_COOKIE}=${user.accessToken}")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserDocument::class.java)
            .returnResult()
            .responseBody

        requireNotNull(responseBody) { "Response has empty body" }

        assertEquals(user.info.email, responseBody.email)
        assertEquals(AccountType.REGISTERED, responseBody.accountType)

        deleteAccount(user)
    }
    @Test fun `getAccount needs authentication`() = runTest {
        webTestClient.get()
            .uri("/account/me")
            .exchange()
            .expectStatus().isUnauthorized
    }
}