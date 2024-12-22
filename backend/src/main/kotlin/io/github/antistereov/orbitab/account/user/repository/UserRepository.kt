package io.github.antistereov.orbitab.account.user.repository

import io.github.antistereov.orbitab.account.user.model.UserDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<UserDocument, String> {

    suspend fun existsByEmail(email: String): Boolean

    suspend fun findByEmail(email: String): UserDocument?
}
