package util

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

suspend inline fun <T, R> T.runSuspendCatching(noinline block: suspend T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (throwable: Throwable) {
        coroutineContext.ensureActive()

        Result.failure(throwable)
    }
}
