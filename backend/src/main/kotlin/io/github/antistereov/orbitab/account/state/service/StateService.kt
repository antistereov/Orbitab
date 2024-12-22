package io.github.antistereov.orbitab.account.state.service

import io.github.antistereov.orbitab.account.state.exception.InvalidStateParameterException
import io.github.antistereov.orbitab.account.state.model.StateParameter
import io.github.antistereov.orbitab.account.state.repository.StateRepository
import io.github.antistereov.orbitab.security.AESEncryption
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StateService(
    private val aesEncryption: AESEncryption,
    private val stateRepository: StateRepository,
) {

    val logger: Logger = LoggerFactory.getLogger(StateService::class.java)
    val validityPeriodMinutes = 5L

    suspend fun createState(userId: String): String {
        val timestamp = Instant.now().toEpochMilli()
        val state = "$userId:$timestamp"
        val encryptedState = aesEncryption.encrypt(state)

        stateRepository.save(StateParameter(encryptedState, userId, timestamp))

        return encryptedState
    }

    suspend fun getUserId(state: String): String {
        val decryptedState = aesEncryption.decrypt(state)
        val storedState = stateRepository.findById(state)
            ?: throw InvalidStateParameterException("State $state not found in database")

        validateState(decryptedState, storedState)

        val userId = decryptedState.split(":")[0]
        val stateTime = getStateTime(decryptedState)

        validateTime(stateTime)

        deleteState(state)

        return userId
    }

    suspend fun deleteExpiredStates() {
        val currentTime = Instant.now()
        var deletedCount = 0

        stateRepository.findAll()
            .filter { state ->
                val stateTime = Instant.ofEpochMilli(state.timestamp)
                stateTime.plusSeconds(validityPeriodMinutes * 60).isBefore(currentTime)
            }
            .onEach { state ->
                deletedCount++
                stateRepository.deleteById(state.id)
            }
            .collect()

        logger.info("$deletedCount expired states were deleted.")
    }

    private fun validateState(decryptedState: String, storedStateParameter: StateParameter) {
        val parts = decryptedState.split(":")
        if (parts.size != 2 || decryptedState != "${storedStateParameter.userId}:${storedStateParameter.timestamp}") {
            throw InvalidStateParameterException("State has an invalid format")
        }
    }

    private fun getStateTime(decryptedState: String): Instant {
        val timestamp = decryptedState.split(":")[1].toLongOrNull()
            ?: throw InvalidStateParameterException("State has an invalid format")
        return Instant.ofEpochMilli(timestamp)
    }

    private fun validateTime(stateTime: Instant) {
        val currentTime = Instant.now()
        if (currentTime.isAfter(stateTime.plusSeconds(validityPeriodMinutes * 60))) {
            throw InvalidStateParameterException("State is expired")
        }
    }

    private suspend fun deleteState(state: String) {
        stateRepository.deleteById(state)
    }
}