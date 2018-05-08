package au.com.gridstone.trainingkotlin

import android.util.Log
import au.com.gridstone.trainingkotlin.GalleryResult.Error
import au.com.gridstone.trainingkotlin.GalleryResult.Loading
import au.com.gridstone.trainingkotlin.GalleryResult.Success
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.Result
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * The resulting state of gallery data.
 */
sealed class GalleryResult {
  object Loading : GalleryResult()
  data class Success(val images: List<Image>) : GalleryResult()
  data class Error(val message: String) : GalleryResult()
}

/**
 * A repository of gallery data; the gateway to data to be used throughout the app.
 *
 * If the application were more complex this object could become a class, allowing the unused vals
 * to be GC'd.
 */
object GalleryRepository {
  private val refreshActions: PublishSubject<Unit> = PublishSubject.create()

  private val getGallery: Observable<GalleryResult> = GalleryApi.imagesForPage(0)
      .map { result ->
        if (result.completelySuccessful) {
          // Take the images from the ApiResult and filter out all albums.
          Success(result.requiredValue.data.filter { !it.is_album })
        } else {
          Error(result.response()?.errorBody()?.toString() ?: "Unknown error")
        }
      }
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .startWith(Loading)

  val results: Observable<GalleryResult> = refreshActions
      // Start with a synthetic refresh request to attempt to retrieve initial data.
      .startWith(Unit)
      // Map refresh actions to gallery requests.
      .switchMap { getGallery }
      // Start with Loading so there's an initial result.
      .startWith(Loading)
      // Filter out any duplicate Results.
      .distinctUntilChanged()
      // Deliver the most recent Result to anyone who subscribes.
      .replay(1)
      .autoConnect()

  fun refresh() {
    refreshActions.onNext(Unit)
  }
}

/**
 * The Imgur gallery API, allowing callers to query a list of images.
 */
private object GalleryApi {
  private const val ENDPOINT = "https://api.imgur.com/3/gallery/"
  private const val CLIENT_ID = "3436c108ccc17d3"

  private val httpClient = OkHttpClient.Builder()
      .addInterceptor { chain ->
        val requestWithAuth = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Client-ID $CLIENT_ID")
            .build()

        chain.proceed(requestWithAuth)
      }
      .addInterceptor(HttpLoggingInterceptor({ message -> Log.v("Http", message) }))
      .build()

  private val webApi = Retrofit.Builder()
      .baseUrl(ENDPOINT)
      .client(httpClient)
      .addConverterFactory(MoshiConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
      .create(GalleryService::class.java)

  fun imagesForPage(page: Int): Single<Result<Gallery>> = webApi.listGallery(page)

  private interface GalleryService {
    @GET("hot/viral/{page}") fun listGallery(@Path("page") page: Int): Single<Result<Gallery>>
  }
}

private val Result<*>.completelySuccessful: Boolean
  get() = !isError && response()?.isSuccessful ?: false

private val <T : Any> Result<T>.requiredValue: T
  get() = requireNotNull(response()?.body())
