package au.com.gridstone.training_kotlin

import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import java.io.IOException

/**
 * A simplification and unification of Retrofit's [Result] and [Response] classes. This is purely
 * for convenience, as querying [Result]'s status and then [Response]'s status is a pain.
 *
 * If [Result.error] is anything other than an [IOException] then this class throws a
 * [RuntimeException] on creation, as it represents a fatal error made by the developer.
 */
data class ApiResult<out T>(val successful: Boolean, val error: String?, val value: T?) {
  constructor(result: Result<T>) : this(
      successful = !result.isError && result.response().isSuccessful,
      error = result.error()?.message ?: result.response()?.errorBody()?.string(),
      value = result.response()?.body()) {

    if (result.isError && result.error() !is IOException) throw RuntimeException(
        "Web request threw something other than an IOException, which means a developer has made an error.",
        result.error())
  }

  /**
   * Sort of like [copy], except allowing you to change the data type of `value`.
   */
  fun <U> changeValue(value: U?): ApiResult<U> = ApiResult(
      successful, error, value)
}
