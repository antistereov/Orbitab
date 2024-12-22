package io.github.antistereov.orbitab.account.guest.service

import io.github.antistereov.orbitab.account.account.exception.AccountDoesNotExistException
import io.github.antistereov.orbitab.account.guest.model.GuestDocument
import io.github.antistereov.orbitab.account.guest.repository.GuestRepository
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class GuestService(
    private val guestRepository: GuestRepository
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun findById(guestId: String): GuestDocument {
        logger.debug { "Finding guest $guestId by ID" }

        return guestRepository.findById(guestId) ?: throw AccountDoesNotExistException(guestId)
    }

    suspend fun findByIdOrNull(guestId: String): GuestDocument? {
        logger.debug { "Finding guest $guestId by ID" }

        return guestRepository.findById(guestId)
    }

    suspend fun save(guest: GuestDocument): GuestDocument {
        logger.debug { "Saving guest ${guest.id}" }

        return guestRepository.save(guest)
    }

    suspend fun deleteById(guestId: String) {
        logger.debug { "Deleting guest $guestId" }

        guestRepository.deleteById(guestId)
    }

    suspend fun findByDeviceIdOrNull(deviceId: String): GuestDocument? {
        logger.debug { "Finding guest by deviceId $deviceId" }

        return guestRepository.findByDeviceId(deviceId)
    }

    suspend fun deleteInactiveGuests() {
        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS)
        val deletedCount = guestRepository.deleteByLastActiveBefore(thirtyDaysAgo) ?: 0L
        logger.info { "Deleted $deletedCount inactive guests who were last active before $thirtyDaysAgo" }
    }
}
