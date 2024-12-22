package io.github.antistereov.orbitab.scheduler

import io.github.antistereov.orbitab.account.state.service.StateService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StateCleanupScheduler(
    private val stateService: StateService
) {

    @Scheduled(fixedRate = 10*60*1000)
    suspend fun deleteExpiredStates() {
        stateService.deleteExpiredStates()
    }
}