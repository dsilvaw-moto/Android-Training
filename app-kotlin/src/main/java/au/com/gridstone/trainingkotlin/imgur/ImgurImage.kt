package au.com.gridstone.trainingkotlin.imgur

import android.os.Parcel
import android.os.Parcelable

data class ImgurImage(val id: String, val type: String,
                      val title: String,
                      val description: String,
                      val link: String,
                      val width: Int,
                      val height: Int) : Parcelable {

  constructor(parcel: Parcel) : this(parcel.readString(),
                                     parcel.readString(),
                                     parcel.readString(),
                                     parcel.readString(),
                                     parcel.readString(),
                                     parcel.readInt(),
                                     parcel.readInt())

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(id)
    parcel.writeString(type)
    parcel.writeString(title)
    parcel.writeString(description)
    parcel.writeString(link)
    parcel.writeInt(width)
    parcel.writeInt(height)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<ImgurImage> {
    override fun createFromParcel(parcel: Parcel): ImgurImage {
      return ImgurImage(parcel)
    }

    override fun newArray(size: Int): Array<ImgurImage?> {
      return arrayOfNulls(size)
    }
  }
}
