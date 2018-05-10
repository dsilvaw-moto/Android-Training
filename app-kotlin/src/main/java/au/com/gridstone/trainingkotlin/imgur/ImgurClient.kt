package au.com.gridstone.trainingkotlin.imgur

import au.com.gridstone.trainingkotlin.imgur.ImgurClient.State.Error
import au.com.gridstone.trainingkotlin.imgur.ImgurClient.State.Handled
import au.com.gridstone.trainingkotlin.imgur.ImgurClient.State.Idle
import au.com.gridstone.trainingkotlin.imgur.ImgurClient.State.Waiting
import au.com.gridstone.trainingkotlin.utils.BuildDateConverter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

interface ImgurClientInterface {

  @Headers("Authorization: Client-ID 3436c108ccc17d3")
  @GET("top/top/day/1")
  fun getLatestImages(): Single<ImgurRequest>

}

object ImgurClient {

  interface Callback {
    fun onDataReady(data: List<ImgurPost>?)
    fun onDataError(error:Throwable)
  }

  enum class State { Idle, Waiting, Handled, Error }

  private const val BASE_ENDPOINT_URL = "https://api.imgur.com/3/gallery/"

  private var delegate: List<ImgurPost>? = null;
  private var callback: Callback? = null
  var state: State = Idle

  private val client: ImgurClientInterface = Retrofit.Builder()
      .baseUrl(BASE_ENDPOINT_URL)
      .addConverterFactory(BuildDateConverter())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ImgurClientInterface::class.java)

  fun loadGalley() {
    state = Waiting
    client.getLatestImages()
        .map { request -> request.data }
        .map { list -> list.filter { !it.is_album && it.type != "video/mp4" } }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::handleRequest, this::handleError)
  }

  fun handleRequest(posts: List<ImgurPost>) {
    state = Handled
    delegate = posts

    callback?.let { it.onDataReady(delegate) }
  }

  fun handleError(toThrow: Throwable) {
    state = Error
    callback?.let { it.onDataError(toThrow) }
  }

//  fun requestPosts(toCall: Callback): State {
//    when (state) {
//      Idle -> {
//        loadGalley()
//      }
//      Handled -> {
//        toCall.onDataReady(delegate)
//      }
//    }
//    return state
//  }

  fun attach(toAttach:Callback){
    callback = toAttach
  }

  fun clear(toClear:Callback){
    if(callback == toClear){
      callback = null;
    }
  }

  fun get(toCall:Callback){
    toCall.onDataReady(delegate)
  }

}
