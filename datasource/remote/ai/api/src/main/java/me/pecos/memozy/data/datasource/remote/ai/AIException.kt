package me.pecos.memozy.data.datasource.remote.ai

sealed class AIException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class NetworkException(message: String, cause: Throwable? = null) :
        AIException(message, cause)

    class RateLimitException(val retryAfterSeconds: Int? = null) :
        AIException("Rate limit exceeded. Retry after ${retryAfterSeconds ?: "unknown"} seconds.")

    class AuthenticationException :
        AIException("Invalid or missing API key.")

    class ServerException(val statusCode: Int, message: String) :
        AIException("Server error ($statusCode): $message")

    class UnknownException(message: String, cause: Throwable? = null) :
        AIException(message, cause)
}
