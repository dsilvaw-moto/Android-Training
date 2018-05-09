package au.com.gridstone.trainingkotlin.utils

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.Date

class DateConverter : TypeAdapter<Date>() {

  override fun read(`in`: JsonReader?): Date {
    val long = `in`!!.nextLong()
    val toReturn = Date(long * 1000)
    return toReturn
  }

  override fun write(
    out: JsonWriter?,
    value: Date?
  ) {
    out!!.value(value!!.time)
  }

}

fun BuildDateConverter(): GsonConverterFactory {
  val gsonBuilder = GsonBuilder()
  gsonBuilder.registerTypeAdapter(Date::class.java, DateConverter())
  val myGson = gsonBuilder.create()
  return GsonConverterFactory.create(myGson)
}