package io.github.antistereov.orbitab.account.guest.service

import io.github.antistereov.orbitab.account.account.exception.AccountDoesNotExistException
import io.github.antistereov.orbitab.account.account.model.GuestDocument
import io.github.antistereov.orbitab.account.guest.repository.GuestRepository
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

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
}
