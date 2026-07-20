package com.boondi.android.data

import com.boondi.android.data.remote.dto.ApiEnvelope
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.io.IOException

/**
 * Shared so the UI (ErrorState) can recognize connectivity failures and render the
 * dedicated offline treatment instead of a generic error.
 */
const val NETWORK_ERROR_MESSAGE = "You're offline — check your connection"

/**
 * Result of a repository call. UI layers switch on this instead of catching exceptions.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val code: String? = null) : ApiResult<Nothing>
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> this
}

/**
 * Wraps a suspending Retrofit call that returns an [ApiEnvelope]. Unwraps `.data` on
 * success and translates network/HTTP failures into [ApiResult.Error] with the backend's
 * `errorCode`/`message` when available.
 */
suspend fun <T : Any> safeApiCall(
    moshi: Moshi,
    call: suspend () -> ApiEnvelope<T>,
): ApiResult<T> = try {
    val envelope = call()
    val data = envelope.data
    if (envelope.success && data != null) {
        ApiResult.Success(data)
    } else {
        ApiResult.Error(envelope.message ?: "Request failed", envelope.errorCode)
    }
} catch (e: HttpException) {
    parseHttpError(moshi, e)
} catch (e: IOException) {
    ApiResult.Error(NETWORK_ERROR_MESSAGE)
} catch (e: Exception) {
    ApiResult.Error(e.message ?: "Something went wrong")
}

/**
 * Like [safeApiCall] but for endpoints whose success payload has no meaningful `data`
 * (the backend sends `ApiResponse.success(null, "...")` — e.g. mark-as-read). Only
 * `envelope.success` is checked, so a null `data` on success isn't misread as a failure.
 */
suspend fun <T> safeApiCallUnit(
    moshi: Moshi,
    call: suspend () -> ApiEnvelope<T>,
): ApiResult<Unit> = try {
    val envelope = call()
    if (envelope.success) {
        ApiResult.Success(Unit)
    } else {
        ApiResult.Error(envelope.message ?: "Request failed", envelope.errorCode)
    }
} catch (e: HttpException) {
    parseHttpError(moshi, e)
} catch (e: IOException) {
    ApiResult.Error(NETWORK_ERROR_MESSAGE)
} catch (e: Exception) {
    ApiResult.Error(e.message ?: "Something went wrong")
}

/** Extract the backend error envelope from a non-2xx HTTP response body. */
fun parseHttpError(moshi: Moshi, e: HttpException): ApiResult.Error {
    val domainFallback = when (e.code()) {
        401 -> "Session expired — please sign in again"
        403 -> "You don't have permission to do that"
        404 -> "Not found"
        409 -> "Already done"
        else -> "Request failed (${e.code()})"
    }
    val raw = e.response()?.errorBody()?.string()
    if (raw.isNullOrBlank()) return ApiResult.Error(domainFallback)
    return try {
        val adapter = moshi.adapter(ApiEnvelope::class.java)
        val env = adapter.fromJson(raw)
        ApiResult.Error(env?.message ?: domainFallback, env?.errorCode)
    } catch (_: Exception) {
        // The body isn't our API's JSON envelope, so this response almost certainly never
        // reached our backend — a proxy/tunnel/CDN error page, not our app returning this
        // status. The domain-specific fallback above (e.g. "Session expired" for 401) would
        // assert something false about the app, so use a generic, honest message instead.
        ApiResult.Error("Couldn't reach the server. Please try again.")
    }
}
