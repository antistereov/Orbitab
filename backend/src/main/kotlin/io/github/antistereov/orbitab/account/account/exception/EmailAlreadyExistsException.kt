package io.github.antistereov.orbitab.account.account.exception

class EmailAlreadyExistsException(info: String) : AccountException(
    message = "$info: Email already exists"
)