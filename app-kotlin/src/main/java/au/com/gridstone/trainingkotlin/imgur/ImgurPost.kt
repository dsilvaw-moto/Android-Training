package au.com.gridstone.trainingkotlin.imgur

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class ImgurPost(
  val title: String,
  val type: String,
  val description: String,
  val datetime: Date,
  val animated: Boolean = false,
  val views: Int,
  val link: String,
  val is_album: Boolean,
  var images: List<ImgurImage>,
  val width: Int,
  val height: Int
) : Parcelable {
  constructor(parcel: Parcel) : this(
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      Date(parcel.readLong()),
      parcel.readByte() != 0.toByte(),
      parcel.readInt(),
      parcel.readString(),
      parcel.readByte() != 0.toByte(),
      parcel.createTypedArrayList(ImgurImage),
      parcel.readInt(),
      parcel.readInt()
  ) {
  }

  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    parcel.writeString(title)
    parcel.writeString(type)
    parcel.writeString(description)
    parcel.writeLong(datetime.time)
    parcel.writeByte(if (animated) 1 else 0)
    parcel.writeInt(views)
    parcel.writeString(link)
    parcel.writeByte(if (is_album) 1 else 0)
    parcel.writeTypedList(images)
    parcel.writeInt(width)
    parcel.writeInt(height)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<ImgurPost> {
    override fun createFromParcel(parcel: Parcel): ImgurPost {
      return ImgurPost(parcel)
    }

    override fun newArray(size: Int): Array<ImgurPost?> {
      return arrayOfNulls(size)
    }
  }
}