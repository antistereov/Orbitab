package io.github.antistereov.orbitab.account.guest.repository

import io.github.antistereov.orbitab.account.account.model.GuestDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface GuestRepository : CoroutineCrudRepository<GuestDocument, String> {

    suspend fun findByDeviceId(deviceId: String): GuestDocument?
}