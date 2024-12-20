package io.github.antistereov.orbitab.account.account.exception

class AccountDoesNotExistException(
    accountId: String
) : AccountException(
    message = "Account $accountId does not exists"
)