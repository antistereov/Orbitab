package io.github.antistereov.orbitab.account.guest.repository

import io.github.antistereov.orbitab.account.guest.model.GuestDocument
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant

interface GuestRepository : CoroutineCrudRepository<GuestDocument, String> {

    suspend fun findByDeviceId(deviceId: String): GuestDocument?

    @Query("{ 'lastActive': { \$lt: ?0 } }")
    suspend fun deleteByLastActiveBefore(date: Instant): Long?
}