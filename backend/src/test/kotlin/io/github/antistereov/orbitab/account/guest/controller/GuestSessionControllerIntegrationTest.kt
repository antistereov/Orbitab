package io.github.antistereov.orbitab.account.guest.controller

import io.github.antistereov.orbitab.BaseIntegrationTest
import io.github.antistereov.orbitab.account.account.model.AccountType
import io.github.antistereov.orbitab.account.guest.model.GuestDocument
import io.github.antistereov.orbitab.config.Constants
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class GuestSessionControllerIntegrationTest : BaseIntegrationTest() {

    @Test fun `getAccount returns guest account`() = runTest {
        val guest = registerGuest()

        val responseBody = webTestClient.get()
            .uri("/guest/me")
            .header(HttpHeaders.COOKIE, "${Constants.ACCESS_TOKEN_COOKIE}=${guest.accessToken}")
            .exchange()
            .expectStatus().isOk
            .expectBody(GuestDocument::class.java)
            .returnResult()
            .responseBody

        requireNotNull(responseBody) { "Response has empty body" }

        assertEquals(guest.info.deviceId, responseBody.deviceId)
        assertEquals(AccountType.GUEST, responseBody.accountType)

        deleteAccount(guest)
    }
    @Test fun `getAccount needs authentication`() = runTest {
        webTestClient.get()
            .uri("/account/me")
            .exchange()
            .expectStatus().isUnauthorized
    }
}