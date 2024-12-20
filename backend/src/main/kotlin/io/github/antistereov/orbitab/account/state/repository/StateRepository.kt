package io.github.antistereov.orbitab.account.state.repository

import io.github.antistereov.orbitab.account.state.model.StateParameter
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StateRepository : CoroutineCrudRepository<StateParameter, String>