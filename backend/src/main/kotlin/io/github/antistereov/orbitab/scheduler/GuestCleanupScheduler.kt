package io.github.antistereov.orbitab.scheduler

import io.github.antistereov.orbitab.account.guest.service.GuestService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class GuestCleanupScheduler(
    private val guestService: GuestService
) {

    @Scheduled(fixedRate = 24*60*60*1000)
    suspend fun scheduleInactiveGuestCleanup() {
        guestService.deleteInactiveGuests()
    }
}