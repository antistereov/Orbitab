package io.github.antistereov.orbitab.account.user.service

import io.github.antistereov.orbitab.account.account.exception.AccountDoesNotExistException
import io.github.antistereov.orbitab.account.user.model.DeviceInfo
import io.github.antistereov.orbitab.account.user.model.UserDocument
import io.github.antistereov.orbitab.account.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun findById(userId: String): UserDocument {
        logger.debug { "Finding user by ID: $userId" }

        return userRepository.findById(userId) ?: throw AccountDoesNotExistException(userId)
    }

    suspend fun findByIdOrNull(userId: String): UserDocument? {
        logger.debug { "Finding user by ID: $userId" }

        return userRepository.findById(userId)
    }

    suspend fun findByEmail(email: String): UserDocument? {
        logger.debug { "Fetching user with email $email" }

        return userRepository.findByEmail(email)
    }

    suspend fun existsByEmail(email: String): Boolean {
        logger.debug { "Checking if email $email already exists" }

        return userRepository.existsByEmail(email)
    }

    suspend fun save(user: UserDocument): UserDocument {
        logger.debug { "Saving user: ${user.id}" }

        return userRepository.save(user)
    }

    suspend fun deleteById(userId: String) {
        logger.debug { "Deleting user $userId" }

        userRepository.deleteById(userId)
    }

    suspend fun getDevices(userId: String): List<DeviceInfo> {
        logger.debug { "Getting devices for user $userId" }

        val user = findById(userId)
        return user.devices
    }

    suspend fun addOrUpdateDevice(userId: String, deviceInfo: DeviceInfo): UserDocument {
        logger.debug { "Adding or updating device ${deviceInfo.id} for user $userId" }

        val user = findById(userId)
        val updatedDevices = user.devices.toMutableList()

        val existingDevice = updatedDevices.find { it.id == deviceInfo.id }
        if (existingDevice != null) {
            updatedDevices.remove(existingDevice)
        }

        updatedDevices.add(deviceInfo)
        return save(user.copy(devices = updatedDevices, lastActive = Instant.now()))
    }

    suspend fun deleteDevice(userId: String, deviceId: String): UserDocument {
        logger.debug { "Deleting device $deviceId for user $userId" }

        val user = findById(userId)
        val updatedDevices = user.devices.filterNot { it.id == deviceId }

        return save(user.copy(devices = updatedDevices, lastActive = Instant.now()))
    }
}