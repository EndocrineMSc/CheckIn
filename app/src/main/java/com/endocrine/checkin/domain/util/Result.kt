package com.endocrine.checkin.domain.util

/** Marker for any typed error carried by a [Result]. */
interface Error

/**
 * A generic, typed result: either [Success] with data or [Error] with a typed error.
 *
 * This app is offline-first and local, so the `Result` wrapper is used sparingly — only where
 * a typed success/failure genuinely adds clarity (e.g. CSV export). Room/DataStore calls
 * elsewhere return plain values.
 */
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.endocrine.checkin.domain.util.Error>(val error: E) : Result<Nothing, E>
}

/** A [Result] that carries no data on success — only success/failure. */
typealias EmptyResult<E> = Result<Unit, E>

inline fun <T, E : Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> = when (this) {
    is Result.Error -> Result.Error(error)
    is Result.Success -> Result.Success(map(data))
}

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> = apply {
    if (this is Result.Success) action(data)
}

inline fun <T, E : Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> = apply {
    if (this is Result.Error) action(error)
}

fun <T, E : Error> Result<T, E>.asEmptyResult(): EmptyResult<E> = map { }
