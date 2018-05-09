package au.com.gridstone.trainingkotlin.imgur

import au.com.gridstone.trainingkotlin.utils.BuildDateConverter
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

interface ImgurClientInterface {

  @Headers("Authorization: Client-ID 3436c108ccc17d3")
  @GET("top/top/day/1")
  fun getLatestImages(): Observable<ImgurRequest>?

}

class ImgurClient {

  companion object {

    const val BASE_ENDPOINT_URL = "https://api.imgur.com/3/gallery/"

    var client: ImgurClientInterface = getInterface()

    fun getInterface(): ImgurClientInterface {
      if (client == null) {
        client = Retrofit.Builder()
            .baseUrl(BASE_ENDPOINT_URL)
            .addConverterFactory(BuildDateConverter())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgurClientInterface::class.java)
      }
      return client;
    }

  }

}
