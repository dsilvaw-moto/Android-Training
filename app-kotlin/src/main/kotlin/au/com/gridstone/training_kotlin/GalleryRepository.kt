package au.com.gridstone.training_kotlin

import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
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
 * An action to take upon gallery data.
 */
sealed class GalleryAction {
  object RequestGallery : GalleryAction()
}

/**
 * The resulting state of gallery data.
 */
sealed class GalleryResult {
  object Idle : GalleryResult()
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
object GalleryData {
  val actions: PublishSubject<GalleryAction> = PublishSubject.create()

  // Take RequestGallery Actions and act upon them, returning a GalleryResult.
  val getGallery = ObservableTransformer<GalleryAction.RequestGallery, GalleryResult> { action ->
    action.flatMap {
      GalleryApi.imagesForPage(0)
          .map { (isSuccessful, errorMessage, value) ->
            if (isSuccessful) GalleryResult.Success(
                // Take the images from the ApiResult and filter out all albums.
                value?.data?.filter { !it.is_album } ?: emptyList())
            else GalleryResult.Error(errorMessage ?: "Unknown error")
          }
          .toObservable()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .startWith(GalleryResult.Loading)
    }
  }

  val results: Observable<GalleryResult> = actions
      // For now, RequestGallery is the only type of action that does anything.
      .ofType<GalleryAction.RequestGallery>()
      // Because we want to display initial data, we start with a synthetic RequestGallery.
      .startWith(GalleryAction.RequestGallery)
      // Transform RequestGallery into GalleryResult.
      .compose(getGallery)
      // Start with a blank state because ¯\_(ツ)_/¯
      .startWith(GalleryResult.Idle)
      // Deliver the most recent Result to anyone who subscribes.
      .replay(1)
      .autoConnect()
}

/**
 * The Imgur gallery API, allowing callers to query a list of images.
 */
object GalleryApi {
  const val ENDPOINT = "https://api.imgur.com/3/gallery/"
  const val CLIENT_ID = "3436c108ccc17d3"

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

  fun imagesForPage(page: Int): Single<ApiResult<Gallery>> = webApi.listGallery(page)
      .map { ApiResult(it) }

  private interface GalleryService {
    @GET("hot/viral/{page}") fun listGallery(@Path("page") page: Int): Single<Result<Gallery>>
  }
}
